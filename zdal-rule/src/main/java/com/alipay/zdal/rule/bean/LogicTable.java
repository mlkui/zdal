/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.rule.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alipay.zdal.common.DBType;
import com.alipay.zdal.rule.LogicTableRule;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.ListSharedElement;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.OneToManyEntry;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.RuleChain;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.SharedElement;
import com.alipay.zdal.rule.ruleengine.entities.abstractentities.TablePropertiesSetter;
import com.alipay.zdal.rule.ruleengine.entities.convientobjectmaker.DatabaseMapProvider;
import com.alipay.zdal.rule.ruleengine.entities.convientobjectmaker.TableMapProvider;
import com.alipay.zdal.rule.ruleengine.entities.inputvalue.CalculationContextInternal;
import com.alipay.zdal.rule.ruleengine.entities.retvalue.TargetDB;
import com.alipay.zdal.rule.ruleengine.rule.Field;
import com.alipay.zdal.rule.ruleengine.rule.ListAbstractResultRule;

/**
 * 一个虚拟表。
 *
 * 没有采用先算表，然后用map对应到对应的数据库去的原因是两个 1.处理分表逻辑和分库逻辑完全不同的情况。如按照id取模+gmt时间的 2.全表full
 * scan查询的时候减少先选出所有可能的表，然后用map对应到数据库操作时的对应次数。 3.允许业务方自由设置表内表名的格式。
 *
 *
 *
 *
 */
public class LogicTable extends ListSharedElement implements Cloneable, LogicTableRule,
                                                 TablePropertiesSetter {
    private static final Logger log                 = LoggerFactory.getLogger(LogicTable.class);
    private OneToManyEntry      oneToManyEntry      = new OneToManyEntry();
    /**
     * 通过简单方法生成databaseMap
     */
    private DatabaseMapProvider databaseMapProvider = null;

    /**
     * 表type
     */
    private DBType              dbType              = DBType.MYSQL;
    /**
     * 允许反向输出
     */
    private boolean             allowReverseOutput;

    /**
     * 唯一键是有优先级的。优先级高的，排在List中前面 每个级别的唯一键，支持多个，类似联合主键的概念。但是，后续其他关联处理，目前不考虑多个唯一键
     * 所有List中的每个Set现在只能有一个元素。即每个级别一个唯一键
     *
     * 当一个SQL到达时，按优先级遍历该list，第一个在sql中都包含的级别，其对应唯一键被返回作为解析后的唯一键（主键）
     */
    private List<String>        uniqueKeys;

    /**
     * 如果当前没有针对该表的自定义库，那么就用生成出来的库直接指定给subSharedElement引用。
     * 如果当前已经存在自定义库，那么将自定义库的数据添加到生成出的Map中(如果key相同 则自定义的database对象会覆盖自动生成出来的对象)
     *
     * 有一种情况不需要考虑，也就是说需求方提出尽管有自定义子database对象，
     * 又有全局database对象，仍然希望全局database对象覆盖自定义子规则(这无意义，何必写子规则呢？）
     * 还有一种情况，有全局对象也有子对象，希望如果有子对象，那么全局拼装出的map失效。
     * 这种情况直接删除全局拼装databaseMap的provider对象就好了。
     *
     * @param generatedDatabaseMap
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Database> buildSubSharedMap(Map<String, Database> generatedDatabaseMap) {
        if (this.subSharedElement == null || this.subSharedElement.size() == 0) {
            // 如果当前没有自定义的表规则，则直接使用生成后的map
            if (log.isDebugEnabled()) {
                log
                    .debug("subShared element is null ,use sharedElement generated by database shared element map provider.");
            }
            return generatedDatabaseMap;
        } else {
            if (log.isDebugEnabled()) {
                log
                    .debug("subSharedElement is not null,putAll current subSharedElement to generated sharedElement");
            }
            Map<String, Database> beAppendMap = (Map<String, Database>) subSharedElement;
            generatedDatabaseMap.putAll(beAppendMap);

        }
        if (log.isDebugEnabled()) {
            log.debug("sub shared element is " + generatedDatabaseMap);
        }
        return generatedDatabaseMap;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public DatabaseMapProvider getDatabaseMapProvider() {
        return databaseMapProvider;
    }

    public Set<RuleChain> getRuleChainSet() {
        return ruleChainSet;
    }

    /**
     * @see com.alipay.zdal.rule.ruleengine.entities.abstractentities.ListSharedElement#init()
     */
    public void init() {
        // 初始化数据库节点
        initDatabaseByDatabaseMapProvider();
        //判断subSharedElement 必须不为空
        if (subSharedElement == null || subSharedElement.isEmpty()) {
            throw new IllegalArgumentException("最少必须指定一个库");
        }
        oneToManyEntry.init();
        //将oneToManyEntry 放到各个dataSource里面去，其实subSharedElement 一开始存放的是<string,database>
        for (Entry<String, ? extends SharedElement> sharedElement : subSharedElement.entrySet()) {
            sharedElement.getValue().put(oneToManyEntry);
        }

        // 初始化规则
        super.init();

        registeRule(ruleChainSet);
        if (log.isDebugEnabled()) {
            log.debug("rule chain set [" + ruleChainSet + "] inited;");
        }
    }

    protected void initDatabaseByDatabaseMapProvider() {
        if (databaseMapProvider != null) {
            Map<String, Database> generatedDatabaseMap = databaseMapProvider.getDatabaseMap();
            if (generatedDatabaseMap != null) {
                subSharedElement = buildSubSharedMap(generatedDatabaseMap);
            }
        }
    }

    /**
     * 值探测器
     */
    private Set<RuleChain> ruleChainSet = new HashSet<RuleChain>();

    public void setDatabase(Map<String, SharedElement> databasesMap) {
        if (databasesMap != null) {
            super.subSharedElement = databasesMap;
        }

    }

    public void setDatabaseMapProvider(DatabaseMapProvider databaseMapProvider) {
        this.databaseMapProvider = databaseMapProvider;
    }

    public void setDatabaseRule(List<ListAbstractResultRule> databaseRule) {
        RuleChainImp ruleChainImp = new RuleChainImp();
        ruleChainImp.setListResultRule(databaseRule);
        ruleChainImp.setDatabaseRuleChain(true);
        ruleChainImp.init();
        this.listResultRule = ruleChainImp;

    }

    /**
     * 返回全库全表
     *
     * @return 非null ，数据源结果集
     *
     */
    @SuppressWarnings("unchecked")
    public List<TargetDB> getAllTargetDBList() {
        List<TargetDB> targetDatabases = new ArrayList<TargetDB>();
        Map<String, Database> beingSelectedDatabases = (Map<String, Database>) getSubSharedElements();
        for (Database database : beingSelectedDatabases.values()) {
            TargetDB db = new TargetDB();
            db.setDbIndex(database.getDataSourceKey());
            Set<String> tempTableNameSet = new HashSet<String>();
            Map<String, SharedElement> tablesMap = database.getTables();
            Collection<? extends SharedElement> tableSharedElement = tablesMap.values();
            for (SharedElement shearedElement : tableSharedElement) {
                Table table = (Table) shearedElement;
                tempTableNameSet.add(table.getTableName());
            }
            db.setTableNames(tempTableNameSet);
            targetDatabases.add(db);
        }
        return targetDatabases;
    }

    // public DBType getDBType() {
    // return dbType;
    // }
    public void setDBType(DBType dbType) {
        this.dbType = dbType;
    }

    public boolean isAllowReverseOutput() {
        return this.allowReverseOutput;
    }

    public void setAllowReverseOutput(boolean allowReverseOutput) {
        this.allowReverseOutput = allowReverseOutput;
    }

    public List<String> getUniqueColumns() {
        return uniqueKeys;
    }

    public List<String> getUniqueKeys() {
        return uniqueKeys;
    }

    public void setUniqueKeys(List<String> uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
    }

    /**
     * 被外部调用的接口。
     * 用于返回分库分表的结果
     *
     * 如果本LogicTable本身没有规则，既对应的logicTable没有分库规则,
     * 或者虽然有规则，但是当前的sql条件中没有匹配到这些规则，分下面两种情况处理：
     *    a 如果logicTale有且只有一个库，那么无论默认规则为何都应该取出该库。这种情况主要出现在单库（单,多）表，或（单，多）库 单表的情况下。
     *    b 如果logicTale有多个库，那么应该使用                   认选库策略（DEFAULT_LIST_RESULT_STRAGETY）。这种情况将策略设为DEFAULT_LIST_RESULT_STRAGETY.NONE比较安全;
     * 如果给定了规则，直接使用规则进行计算。
     */
    public List<TargetDB> calculate(Map<RuleChain, CalculationContextInternal> map) {
        CalculationContextInternal calculationContext = map.get(this.listResultRule);
        Map<String/* 结果的值 */, Field> resultmap = null;
        if (calculationContext == null) {
            //规则为空，或者规则未匹配到
            if (subSharedElement != null && subSharedElement.size() == 1) {
                // 1）在规则未匹配到并且sharedElement唯一的情况下，默认的选择唯一的sharedElement.
                //这时候如果规则为NONE，也会选择唯一的，不会使用默认规则。
                resultmap = buildSingleDatabase();
            } else {
                //2)规则为空，规则未匹配到，并且默认子库不为唯一的一个的情况下，使用默认规则。
                resultmap = buildDefaultDatabaseMap();
            }
        } else {
            //3）
            ListAbstractResultRule rule = calculationContext.ruleChain
                .getRuleByIndex(calculationContext.index);
            resultmap = rule.eval(calculationContext.sqlArgs);
        }

        List<TargetDB> targetDBList = buildTargetDBList(map, resultmap);

        return targetDBList;
    }

    final Map<String, Field> buildDefaultDatabaseMap() {
        Map<String, Field> resultmap;
        resultmap = new HashMap<String, Field>(defaultListResult.size());
        for (String defaultIndex : defaultListResult) {
            resultmap.put(defaultIndex, Field.EMPTY_FIELD);
        }
        return resultmap;
    }

    final List<TargetDB> buildTargetDBList(Map<RuleChain, CalculationContextInternal> map,
                                           Map<String, Field> resultmap) {
        List<TargetDB> targetDBList = new ArrayList<TargetDB>();//表名的列表
        for (Entry<String, Field> entry : resultmap.entrySet()) {
            Database database = (Database) subSharedElement.get(entry.getKey());
            if (database == null) {
                throw new IllegalArgumentException("不能根据key: " + entry.getKey()
                                                   + "取到对应的表名，当前子规则中所有表名为:" + subSharedElement);
            } else {
                //添加表名
                TargetDB targetDB = new TargetDB();
                String dbKey = database.getDataSourceKey();
                if (dbKey == null) {
                    throw new IllegalArgumentException("database对象不包含数据源key id");
                }
                targetDB.setDbIndex(dbKey);
                database.calculateTable(targetDB, entry.getValue(), map);
                targetDBList.add(targetDB);
            }

        }
        return targetDBList;
    }

    final Map<String, Field> buildSingleDatabase() {
        Map<String, Field> resultmap;
        resultmap = new HashMap<String, Field>(1);
        for (String key : subSharedElement.keySet()) {
            resultmap.put(key, Field.EMPTY_FIELD);
        }
        return resultmap;
    }

    public void setLogicTableName(String logicTable) {
        oneToManyEntry.setLogicTableName(logicTable);
    }

    public String getLogicTableName() {
        return oneToManyEntry.getLogicTableName();
    }

    public void setTableRuleChain(RuleChain ruleChain) {
        oneToManyEntry.setTableRuleChain(ruleChain);
    }

    public void setTableMapProvider(TableMapProvider tableMapProvider) {
        oneToManyEntry.setTableMapProvider(tableMapProvider);
    }

    public TableMapProvider getTableMapProvider() {
        return oneToManyEntry.getTableMapProvider();
    }

    @Override
    public String toString() {
        return "LogicTable [allowReverseOutput=" + allowReverseOutput + ", databaseMapProvider="
               + databaseMapProvider + ", dbType=" + dbType + ", uniqueKeys=" + uniqueKeys
               + ", defaultListResult=" + defaultListResult + ", defaultListResultStragety="
               + defaultListResultStragety + ", listResultRule=" + listResultRule
               + ", subSharedElement=" + subSharedElement + "]";
    }

    public void setDefaultListResultStragety(DEFAULT_LIST_RESULT_STRAGETY defaultListResultStragety) {
        // 重载了父类的同名函数，
        super.setDefaultListResultStragety(defaultListResultStragety);
        oneToManyEntry.setDefaultListResultStragety(defaultListResultStragety);
    }

    public void setTableRule(List<ListAbstractResultRule> tableRule) {
        oneToManyEntry.setTableRule(tableRule);
    }

    public List<ListAbstractResultRule> getTableRule() {
        return oneToManyEntry.getTableRule();
    }

}
