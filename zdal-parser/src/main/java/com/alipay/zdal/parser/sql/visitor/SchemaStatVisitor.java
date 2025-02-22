/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.zdal.parser.sql.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alipay.zdal.parser.sql.ast.SQLExpr;
import com.alipay.zdal.parser.sql.ast.SQLName;
import com.alipay.zdal.parser.sql.ast.SQLObject;
import com.alipay.zdal.parser.sql.ast.SQLOrderBy;
import com.alipay.zdal.parser.sql.ast.SQLOrderingSpecification;
import com.alipay.zdal.parser.sql.ast.expr.SQLAggregateExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLAllColumnExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLBinaryOpExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLCurrentOfCursorExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLIdentifierExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLInListExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLInSubQueryExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLIntegerExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLMethodInvokeExpr;
import com.alipay.zdal.parser.sql.ast.expr.SQLPropertyExpr;
import com.alipay.zdal.parser.sql.ast.statement.SQLAlterTableAddColumn;
import com.alipay.zdal.parser.sql.ast.statement.SQLAlterTableStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLCallStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLColumnDefinition;
import com.alipay.zdal.parser.sql.ast.statement.SQLCommentStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLCreateTableStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLDeleteStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLDropTableStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLExprTableSource;
import com.alipay.zdal.parser.sql.ast.statement.SQLInsertStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLJoinTableSource;
import com.alipay.zdal.parser.sql.ast.statement.SQLSelect;
import com.alipay.zdal.parser.sql.ast.statement.SQLSelectItem;
import com.alipay.zdal.parser.sql.ast.statement.SQLSelectOrderByItem;
import com.alipay.zdal.parser.sql.ast.statement.SQLSelectQuery;
import com.alipay.zdal.parser.sql.ast.statement.SQLSelectQueryBlock;
import com.alipay.zdal.parser.sql.ast.statement.SQLSelectStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLSubqueryTableSource;
import com.alipay.zdal.parser.sql.ast.statement.SQLTableElement;
import com.alipay.zdal.parser.sql.ast.statement.SQLTruncateStatement;
import com.alipay.zdal.parser.sql.ast.statement.SQLUpdateStatement;
import com.alipay.zdal.parser.sql.stat.TableStat;
import com.alipay.zdal.parser.sql.stat.TableStat.Column;
import com.alipay.zdal.parser.sql.stat.TableStat.Condition;
import com.alipay.zdal.parser.sql.stat.TableStat.Mode;
import com.alipay.zdal.parser.sql.stat.TableStat.Relationship;
import com.alipay.zdal.parser.sql.stat.TableStat.SELECTMODE;

/**
 * 
 * @author 伯牙
 * @version $Id: SchemaStatVisitor.java, v 0.1 2012-11-17 下午3:56:15 Exp $
 */
public class SchemaStatVisitor extends SQLASTVisitorAdapter {

    protected final HashMap<TableStat.Name, TableStat> tableStats      = new LinkedHashMap<TableStat.Name, TableStat>();
    protected final Set<Column>                        columns         = new LinkedHashSet<Column>();
    protected final List<Condition>                    conditions      = new ArrayList<Condition>();
    protected final Set<Relationship>                  relationships   = new LinkedHashSet<Relationship>();
    protected final List<Column>                       orderByColumns  = new ArrayList<Column>();
    protected final Set<Column>                        groupByColumns  = new LinkedHashSet<Column>();

    protected final Map<String, SQLObject>             subQueryMap     = new LinkedHashMap<String, SQLObject>();

    protected final Map<String, SQLObject>             variants        = new LinkedHashMap<String, SQLObject>();

    protected Map<String, String>                      aliasMap        = new HashMap<String, String>();

    protected String                                   currentTable;

    public final static String                         ATTR_TABLE      = "_table_";
    public final static String                         ATTR_COLUMN     = "_column_";

    private List<Object>                               parameters;

    private Mode                                       mode;

    /** 保留select count,select min,select max,select sum格式的查询语句. */
    private SELECTMODE                                 selectMode;

    /**从DB2的sql语句中中获取分页字段.  */
    protected String                                   limitColumnName = null;

    public SELECTMODE getSelectMode() {
        return selectMode;
    }

    public void setSelectMode(SELECTMODE selectMode) {
        this.selectMode = selectMode;
    }

    public SchemaStatVisitor() {
        this(new ArrayList<Object>());
    }

    public SchemaStatVisitor(List<Object> parameters) {
        this.parameters = parameters;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

    public TableStat getTableStat(String ident) {
        return getTableStat(ident, null);
    }

    public Column addColumn(String tableName, String columnName) {
        tableName = handleName(tableName);
        columnName = handleName(columnName);

        Column column = new Column(tableName, columnName);
        columns.add(column);
        return column;
    }

    public TableStat getTableStat(String tableName, String alias) {
        if (variants.containsKey(tableName)) {
            return null;
        }

        tableName = handleName(tableName);
        TableStat stat = tableStats.get(tableName);
        if (stat == null) {
            stat = new TableStat();
            tableStats.put(new TableStat.Name(tableName), stat);
            if (alias != null) {
                aliasMap.put(alias, tableName);
            }
        }
        return stat;
    }

    private String handleName(String ident) {
        ident = ident.replaceAll("\"", "");
        ident = ident.replaceAll(" ", "");

        ident = aliasWrap(ident);

        return ident;
    }

    public Map<String, SQLObject> getVariants() {
        return variants;
    }

    public void setAliasMap() {
        this.setAliasMap(new HashMap<String, String>());
    }

    public void clearAliasMap() {
        this.aliasMap = null;
    }

    public void setAliasMap(Map<String, String> aliasMap) {
        this.aliasMap = aliasMap;
    }

    public Map<String, String> getAliasMap() {
        return aliasMap;
    }

    public void setCurrentTable(String table) {
        this.currentTable = table;
    }

    public void setCurrentTable(SQLObject x) {
        x.putAttribute("_old_local_", this.currentTable);
    }

    public void restoreCurrentTable(SQLObject x) {
        String table = (String) x.getAttribute("_old_local_");
        this.currentTable = table;
    }

    public void setCurrentTable(SQLObject x, String table) {
        x.putAttribute("_old_local_", this.currentTable);
        this.currentTable = table;
    }

    public String getCurrentTable() {
        return currentTable;
    }

    protected Mode getMode() {
        return mode;
    }

    protected void setModeOrigin(SQLObject x) {
        Mode originalMode = (Mode) x.getAttribute("_original_use_mode");
        mode = originalMode;
    }

    protected Mode setMode(SQLObject x, Mode mode) {
        Mode oldMode = this.mode;
        x.putAttribute("_original_use_mode", oldMode);
        this.mode = mode;
        return oldMode;
    }

    public class OrderByStatVisitor extends SQLASTVisitorAdapter {

        private final SQLOrderBy orderBy;

        public OrderByStatVisitor(SQLOrderBy orderBy) {
            this.orderBy = orderBy;
            for (SQLSelectOrderByItem item : orderBy.getItems()) {
                item.getExpr().setParent(item);
            }
        }

        public SQLOrderBy getOrderBy() {
            return orderBy;
        }

        public boolean visit(SQLIdentifierExpr x) {
            String currentTable = getCurrentTable();

            if (subQueryMap.containsKey(currentTable)) {
                return false;
            }

            if (currentTable != null) {
                addOrderByColumn(currentTable, x.getName(), x);
            } else {
                addOrderByColumn("UNKOWN", x.getName(), x);
            }
            return false;
        }

        public boolean visit(SQLPropertyExpr x) {
            if (x.getOwner() instanceof SQLIdentifierExpr) {
                String owner = ((SQLIdentifierExpr) x.getOwner()).getName();

                if (subQueryMap.containsKey(owner)) {
                    return false;
                }

                owner = aliasWrap(owner);

                if (owner != null) {
                    addOrderByColumn(owner, x.getName(), x);
                }
            }

            return false;
        }

        public void addOrderByColumn(String table, String columnName, SQLObject expr) {
            Column column = new Column(table, columnName);

            SQLObject parent = expr.getParent();
            if (parent instanceof SQLSelectOrderByItem) {
                SQLOrderingSpecification type = ((SQLSelectOrderByItem) parent).getType();
                column.getAttributes().put("orderBy.type", type);
            }

            orderByColumns.add(column);
        }
    }

    public boolean visit(SQLOrderBy x) {
        OrderByStatVisitor orderByVisitor = new OrderByStatVisitor(x);
        SQLSelectQueryBlock query = null;
        if (x.getParent() instanceof SQLSelectQueryBlock) {
            query = (SQLSelectQueryBlock) x.getParent();
        }
        if (query != null) {
            for (SQLSelectOrderByItem item : x.getItems()) {
                SQLExpr expr = item.getExpr();
                if (expr instanceof SQLIntegerExpr) {
                    int intValue = ((SQLIntegerExpr) expr).getNumber().intValue() - 1;
                    if (intValue < query.getSelectList().size()) {
                        SQLSelectItem selectItem = query.getSelectList().get(intValue);
                        selectItem.getExpr().accept(orderByVisitor);
                    }
                }
            }
        }
        x.accept(orderByVisitor);
        return true;
    }

    public Set<Relationship> getRelationships() {
        return relationships;
    }

    public List<Column> getOrderByColumns() {
        return orderByColumns;
    }

    public Set<Column> getGroupByColumns() {
        return groupByColumns;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public boolean visit(SQLBinaryOpExpr x) {
        x.getLeft().setParent(x);
        x.getRight().setParent(x);

        switch (x.getOperator()) {
            case Equality:
            case NotEqual:
            case GreaterThan:
            case GreaterThanOrEqual:
            case LessThan:
            case LessThanOrEqual:
            case LessThanOrEqualOrGreaterThan:
            case Like:
            case NotLike:
            case Is:
            case IsNot:
                handleCondition(x.getLeft(), x.getOperator().name, x.getRight());
                handleCondition(x.getRight(), x.getOperator().name, x.getLeft());

                handleRelationship(x.getLeft(), x.getOperator().name, x.getRight());
                break;
            default:
                break;
        }
        return true;
    }

    protected void handleRelationship(SQLExpr left, String operator, SQLExpr right) {
        Column leftColumn = getColumn(left);
        if (leftColumn == null) {
            return;
        }

        Column rightColumn = getColumn(right);
        if (rightColumn == null) {
            return;
        }

        Relationship relationship = new Relationship();
        relationship.setLeft(leftColumn);
        relationship.setRight(rightColumn);
        relationship.setOperator(operator);

        this.relationships.add(relationship);
    }

    protected void handleCondition(SQLExpr expr, String operator, List<SQLExpr> values) {
        handleCondition(expr, operator, values.toArray(new SQLExpr[values.size()]));
    }

    protected void handleCondition(SQLExpr expr, String operator, SQLExpr... valueExprs) {
        Column column = getColumn(expr);
        if (column == null) {
            return;
        }

        Condition condition = null;
        for (Condition item : this.getConditions()) {
            if (item.getColumn().equals(column) && item.getOperator().equals(operator)) {
                condition = item;
                break;
            }
        }

        if (condition == null) {
            condition = new Condition();
            condition.setColumn(column);
            condition.setOperator(operator);
            this.conditions.add(condition);
        }

        for (SQLExpr item : valueExprs) {
            if (item.toString().equals("?")) {
                condition.getValues().add("?");
            } else {
                Object value = SQLEvalVisitorUtils.eval(getDbType(), item, parameters, false);
                condition.getValues().add(value);
            }
        }
    }

    public String getDbType() {
        return null;
    }

    protected Column getColumn(SQLExpr expr) {
        Map<String, String> aliasMap = getAliasMap();
        if (aliasMap == null) {
            return null;
        }

        if (expr instanceof SQLPropertyExpr) {
            SQLExpr owner = ((SQLPropertyExpr) expr).getOwner();
            String column = ((SQLPropertyExpr) expr).getName();

            if (owner instanceof SQLIdentifierExpr) {
                String tableName = ((SQLIdentifierExpr) owner).getName();
                String table = tableName;
                if (aliasMap.containsKey(table)) {
                    table = aliasMap.get(table);
                }

                if (variants.containsKey(table)) {
                    return null;
                }

                if (table != null) {
                    return new Column(table, column);
                }

                return handleSubQueryColumn(tableName, column);
            }

            return null;
        }

        if (expr instanceof SQLIdentifierExpr) {
            Column attrColumn = (Column) expr.getAttribute(ATTR_COLUMN);
            if (attrColumn != null) {
                return attrColumn;
            }

            String column = ((SQLIdentifierExpr) expr).getName();
            String table = getCurrentTable();
            if (table != null) {
                if (aliasMap.containsKey(table)) {
                    table = aliasMap.get(table);
                    if (table == null) {
                        return null;
                    }
                }
            }

            if (table != null) {
                return new Column(table, column);
            }

            if (variants.containsKey(column)) {
                return null;
            }

            return new Column("UNKNOWN", column);
        }

        return null;
    }

    @Override
    public boolean visit(SQLTruncateStatement x) {
        setMode(x, Mode.Delete);

        setAliasMap();

        String originalTable = getCurrentTable();

        for (SQLExprTableSource tableSource : x.getTableSources()) {
            SQLName name = (SQLName) tableSource.getExpr();

            String ident = name.toString();
            setCurrentTable(ident);
            x.putAttribute("_old_local_", originalTable);

            TableStat stat = getTableStat(ident);
            stat.incrementDeleteCount();

            Map<String, String> aliasMap = getAliasMap();
            if (aliasMap != null) {
                aliasMap.put(ident, ident);
            }
        }

        return false;
    }

    @Override
    public boolean visit(SQLDropTableStatement x) {
        setMode(x, Mode.Insert);

        setAliasMap();

        String originalTable = getCurrentTable();

        for (SQLExprTableSource tableSource : x.getTableSources()) {
            SQLName name = (SQLName) tableSource.getExpr();
            String ident = name.toString();
            setCurrentTable(ident);
            x.putAttribute("_old_local_", originalTable);

            TableStat stat = getTableStat(ident);
            stat.incrementDropCount();

            Map<String, String> aliasMap = getAliasMap();
            if (aliasMap != null) {
                aliasMap.put(ident, ident);
            }
        }

        return false;
    }

    @Override
    public boolean visit(SQLInsertStatement x) {
        setMode(x, Mode.Insert);

        setAliasMap();

        String originalTable = getCurrentTable();

        if (x.getTableName() instanceof SQLName) {
            String ident = ((SQLName) x.getTableName()).toString();
            setCurrentTable(ident);
            x.putAttribute("_old_local_", originalTable);

            TableStat stat = getTableStat(ident);
            stat.incrementInsertCount();

            Map<String, String> aliasMap = getAliasMap();
            if (aliasMap != null) {
                if (x.getAlias() != null) {
                    aliasMap.put(x.getAlias(), ident);
                }
                aliasMap.put(ident, ident);
            }
        }

        accept(x.getColumns());
        accept(x.getQuery());

        return false;
    }

    protected void accept(SQLObject x) {
        if (x != null) {
            x.accept(this);
        }
    }

    protected void accept(List<? extends SQLObject> nodes) {
        for (int i = 0, size = nodes.size(); i < size; ++i) {
            accept(nodes.get(i));
        }
    }

    public boolean visit(SQLSelectQueryBlock x) {
        if (x.getSelectList() != null && x.getSelectList().size() > 0) {
            List<SQLSelectItem> selectItems = x.getSelectList();
            List<SQLAggregateExpr> aggregateExprs = new ArrayList<SQLAggregateExpr>();
            for (SQLSelectItem sqlSelectItem : selectItems) {
                SQLExpr sqlExpr = sqlSelectItem.getExpr();
                if (sqlExpr instanceof SQLAggregateExpr) {
                    SQLAggregateExpr aggregateExpr = (SQLAggregateExpr) sqlExpr;
                    aggregateExprs.add(aggregateExpr);
                }
            }
            if (aggregateExprs.size() == 0) {

            } else if (aggregateExprs.size() > 1) {//只支持单个聚合函数.
                setSelectMode(null);
            } else {
                SQLAggregateExpr aggregateExpr = aggregateExprs.get(0);
                if (SELECTMODE.COUNT.toString().equalsIgnoreCase(aggregateExpr.getMethodName())) {
                    if (getSelectMode() == null) {//如果外层已经有聚合函数，就不需要再次设置了，以外层的为准.
                        setSelectMode(SELECTMODE.COUNT);
                    }
                } else if (SELECTMODE.MIN.toString()
                    .equalsIgnoreCase(aggregateExpr.getMethodName())) {
                    if (getSelectMode() == null) {
                        setSelectMode(SELECTMODE.MIN);
                    }
                } else if (SELECTMODE.MAX.toString()
                    .equalsIgnoreCase(aggregateExpr.getMethodName())) {
                    if (getSelectMode() == null) {
                        setSelectMode(SELECTMODE.MAX);
                    }
                } else if (SELECTMODE.SUM.toString()
                    .equalsIgnoreCase(aggregateExpr.getMethodName())) {
                    if (getSelectMode() == null) {
                        setSelectMode(SELECTMODE.SUM);
                    }
                } else if (SELECTMODE.ROWNUMBER.toString().equalsIgnoreCase(
                    aggregateExpr.getMethodName())) {
                    if (aggregateExpr.getParent() != null
                        && aggregateExpr.getParent() instanceof SQLSelectItem) {
                        SQLSelectItem sqlSelectItem = (SQLSelectItem) aggregateExpr.getParent();
                        if (this.limitColumnName == null) {
                            this.limitColumnName = sqlSelectItem.getAlias();
                        }
                    }
                } else {
                    //                    setSelectMode(null);
                }
            }
        }
        setMode(x, Mode.Select);

        if (x.getFrom() instanceof SQLSubqueryTableSource) {
            x.getFrom().accept(this);
            return false;
        }

        if (x.getInto() != null) {
            if (x.getInto().getExpr() instanceof SQLName) {
                SQLName into = (SQLName) x.getInto().getExpr();
                String ident = into.toString();
                TableStat stat = getTableStat(ident);
                if (stat != null) {
                    stat.incrementInsertCount();
                }
            }
        }

        String originalTable = getCurrentTable();

        if (x.getFrom() instanceof SQLExprTableSource) {
            SQLExprTableSource tableSource = (SQLExprTableSource) x.getFrom();
            if (tableSource.getExpr() instanceof SQLName) {
                String ident = tableSource.getExpr().toString();

                setCurrentTable(x, ident);
                x.putAttribute(ATTR_TABLE, ident);
                if (x.getParent() instanceof SQLSelect) {
                    x.getParent().putAttribute(ATTR_TABLE, ident);
                }
                x.putAttribute("_old_local_", originalTable);
            }
        }

        if (x.getFrom() != null) {
            x.getFrom().accept(this); // 提前执行，获得aliasMap
            String table = (String) x.getFrom().getAttribute(ATTR_TABLE);
            if (table != null) {
                x.putAttribute(ATTR_TABLE, table);
            }
        }

        // String ident = x.getTable().toString();
        //
        // TableStat stat = getTableStat(ident);
        // stat.incrementInsertCount();
        // return false;

        if (x.getWhere() != null) {
            x.getWhere().setParent(x);
        }

        return true;
    }

    public void endVisit(SQLSelectQueryBlock x) {
        String originalTable = (String) x.getAttribute("_old_local_");
        x.putAttribute("table", getCurrentTable());
        setCurrentTable(originalTable);

        setModeOrigin(x);
    }

    public boolean visit(SQLJoinTableSource x) {
        return true;
    }

    public boolean visit(SQLPropertyExpr x) {
        if (x.getOwner() instanceof SQLIdentifierExpr) {
            String owner = ((SQLIdentifierExpr) x.getOwner()).getName();

            if (subQueryMap.containsKey(owner)) {
                return false;
            }

            owner = aliasWrap(owner);

            if (owner != null) {
                Column column = addColumn(owner, x.getName());
                x.putAttribute(ATTR_COLUMN, column);
            }
        }
        return false;
    }

    protected String aliasWrap(String name) {
        Map<String, String> aliasMap = getAliasMap();

        if (aliasMap != null) {
            for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                if (entry.getKey().equalsIgnoreCase(name)) {
                    return entry.getValue();
                }
            }
        }

        return name;
    }

    protected Column handleSubQueryColumn(String owner, String alias) {
        SQLObject query = subQueryMap.get(owner);

        if (query == null) {
            return null;
        }

        List<SQLSelectItem> selectList = null;
        if (query instanceof SQLSelectQueryBlock) {
            selectList = ((SQLSelectQueryBlock) query).getSelectList();
        }

        if (selectList != null) {
            for (SQLSelectItem item : selectList) {
                String itemAlias = item.getAlias();
                SQLExpr itemExpr = item.getExpr();
                if (itemAlias == null) {
                    if (itemExpr instanceof SQLIdentifierExpr) {
                        itemAlias = itemExpr.toString();
                    } else if (itemExpr instanceof SQLPropertyExpr) {
                        itemAlias = ((SQLPropertyExpr) itemExpr).getName();
                    }
                }

                if (alias.equalsIgnoreCase(itemAlias)) {
                    Column column = (Column) itemExpr.getAttribute(ATTR_COLUMN);
                    return column;
                }

            }
        }

        return null;
    }

    public boolean visit(SQLIdentifierExpr x) {
        String currentTable = getCurrentTable();

        if (subQueryMap.containsKey(currentTable)) {
            return false;
        }

        String ident = x.toString();

        if (variants.containsKey(ident)) {
            return false;
        }

        if (currentTable != null) {
            Column column = addColumn(currentTable, ident);
            x.putAttribute(ATTR_COLUMN, column);
        } else {
            Column column = handleUnkownColumn(ident);
            if (column != null) {
                x.putAttribute(ATTR_COLUMN, column);
            }
        }
        return false;
    }

    protected Column handleUnkownColumn(String columnName) {
        return addColumn("UNKNOWN", columnName);
    }

    public boolean visit(SQLAllColumnExpr x) {
        String currentTable = getCurrentTable();

        if (subQueryMap.containsKey(currentTable)) {
            return false;
        }

        if (currentTable != null) {
            addColumn(currentTable, "*");
        }
        return false;
    }

    public Map<TableStat.Name, TableStat> getTables() {
        return tableStats;
    }

    public boolean containsTable(String tableName) {
        return tableStats.containsKey(new TableStat.Name(tableName));
    }

    public Set<Column> getColumns() {
        return columns;
    }

    public boolean visit(SQLSelectStatement x) {
        setAliasMap();
        return true;
    }

    public void endVisit(SQLSelectStatement x) {
        clearAliasMap();
    }

    public boolean visit(SQLSubqueryTableSource x) {
        x.getSelect().accept(this);

        SQLSelectQuery query = x.getSelect().getQuery();

        Map<String, String> aliasMap = getAliasMap();
        if (aliasMap != null) {
            if (x.getAlias() != null) {
                aliasMap.put(x.getAlias(), null);
                subQueryMap.put(x.getAlias(), query);
            }
        }
        return false;
    }

    protected boolean isSimpleExprTableSource(SQLExprTableSource x) {
        return x.getExpr() instanceof SQLName;
    }

    public boolean visit(SQLExprTableSource x) {
        if (isSimpleExprTableSource(x)) {
            String ident = x.getExpr().toString();

            if (variants.containsKey(ident)) {
                return false;
            }

            if (subQueryMap.containsKey(ident)) {
                return false;
            }

            Map<String, String> aliasMap = getAliasMap();

            TableStat stat = getTableStat(ident);

            Mode mode = getMode();
            switch (mode) {
                case Delete:
                    stat.incrementDeleteCount();
                    break;
                case Insert:
                    stat.incrementInsertCount();
                    break;
                case Update:
                    stat.incrementUpdateCount();
                    break;
                case Select:
                    stat.incrementSelectCount();
                    break;
                case Merge:
                    stat.incrementMergeCount();
                    break;
                default:
                    break;
            }

            if (aliasMap != null) {
                String alias = x.getAlias();
                if (alias != null && !aliasMap.containsKey(alias)) {
                    aliasMap.put(alias, ident);
                }
                if (!aliasMap.containsKey(ident)) {
                    aliasMap.put(ident, ident);
                }
            }
        } else {
            accept(x.getExpr());
        }

        return false;
    }

    public boolean visit(SQLSelectItem x) {
        x.getExpr().setParent(x);
        return true;
    }

    public void endVisit(SQLSelect x) {
        restoreCurrentTable(x);
    }

    public boolean visit(SQLSelect x) {
        setCurrentTable(x);

        if (x.getOrderBy() != null) {
            x.getOrderBy().setParent(x);
        }

        accept(x.getQuery());

        String originalTable = getCurrentTable();

        setCurrentTable((String) x.getQuery().getAttribute("table"));
        x.putAttribute("_old_local_", originalTable);

        accept(x.getOrderBy());

        setCurrentTable(originalTable);

        return false;
    }

    public boolean visit(SQLAggregateExpr x) {
        accept(x.getArguments());
        return false;
    }

    public boolean visit(SQLMethodInvokeExpr x) {
        accept(x.getParameters());
        return false;
    }

    public boolean visit(SQLUpdateStatement x) {
        setAliasMap();

        String ident = x.getTableName().toString();
        setCurrentTable(ident);

        TableStat stat = getTableStat(ident);
        stat.incrementUpdateCount();

        Map<String, String> aliasMap = getAliasMap();
        aliasMap.put(ident, ident);

        accept(x.getItems());
        accept(x.getWhere());

        return false;
    }

    public boolean visit(SQLDeleteStatement x) {
        setAliasMap();

        setMode(x, Mode.Delete);

        String tableName = x.getTableName().toString();
        setCurrentTable(tableName);

        if (x.getAlias() != null) {
            this.aliasMap.put(x.getAlias(), tableName);
        }

        TableStat stat = getTableStat(tableName);
        stat.incrementDeleteCount();

        accept(x.getWhere());

        return false;
    }

    public boolean visit(SQLInListExpr x) {
        if (x.isNot()) {
            handleCondition(x.getExpr(), "NOT IN", x.getTargetList());
        } else {
            handleCondition(x.getExpr(), "IN", x.getTargetList());
        }

        return true;
    }

    @Override
    public boolean visit(SQLInSubQueryExpr x) {
        if (x.isNot()) {
            handleCondition(x.getExpr(), "NOT IN");
        } else {
            handleCondition(x.getExpr(), "IN");
        }
        return true;
    }

    public void endVisit(SQLDeleteStatement x) {
        clearAliasMap();
    }

    public void endVisit(SQLUpdateStatement x) {
        clearAliasMap();
    }

    public boolean visit(SQLCreateTableStatement x) {
        for (SQLTableElement e : x.getTableElementList()) {
            e.setParent(x);
        }

        String tableName = x.getName().toString();

        TableStat stat = getTableStat(tableName);
        stat.incrementCreateCount();
        setCurrentTable(x, tableName);

        accept(x.getTableElementList());

        restoreCurrentTable(x);

        return false;
    }

    public boolean visit(SQLColumnDefinition x) {
        String tableName = null;
        {
            SQLObject parent = x.getParent();
            if (parent instanceof SQLCreateTableStatement) {
                tableName = ((SQLCreateTableStatement) parent).getName().toString();
            }
        }

        if (tableName == null) {
            return true;
        }

        String columnName = x.getName().toString();
        addColumn(tableName, columnName);

        return false;
    }

    @Override
    public boolean visit(SQLCallStatement x) {
        return false;
    }

    @Override
    public void endVisit(SQLCommentStatement x) {

    }

    @Override
    public boolean visit(SQLCommentStatement x) {
        return false;
    }

    public boolean visit(SQLCurrentOfCursorExpr x) {
        return false;
    }

    @Override
    public boolean visit(SQLAlterTableAddColumn x) {
        SQLAlterTableStatement stmt = (SQLAlterTableStatement) x.getParent();
        String table = stmt.getName().toString();

        for (SQLColumnDefinition column : x.getColumns()) {
            String columnName = column.getName().toString();
            addColumn(table, columnName);
        }
        return false;
    }

    @Override
    public void endVisit(SQLAlterTableAddColumn x) {

    }
}
