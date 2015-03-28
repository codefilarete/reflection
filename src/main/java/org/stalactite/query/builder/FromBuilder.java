package org.stalactite.query.builder;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.stalactite.lang.StringAppender;
import org.stalactite.lang.Strings;
import org.stalactite.lang.collection.Iterables;
import org.stalactite.persistence.structure.Table;
import org.stalactite.query.model.From;
import org.stalactite.query.model.From.AliasedTable;
import org.stalactite.query.model.From.ColumnJoin;
import org.stalactite.query.model.From.Join;
import org.stalactite.query.model.From.RawTableJoin;

/**
 * @author Guillaume Mary
 */
public class FromBuilder extends AbstractDMLBuilder implements SQLBuilder {

	private static final String INNER_JOIN = " inner join ";
	private static final String LEFT_OUTER_JOIN = " left outer join ";
	private static final String RIGHT_OUTER_JOIN = " right outer join ";
	private final From from;
	private final DiffAliasedTableComparator diffAliasedTableComparator;

	public FromBuilder(From from) {
		super(from.getTableAliases());
		this.from = from;
		this.diffAliasedTableComparator = new DiffAliasedTableComparator();
	}

	@Override
	public String toSQL() {
		StringAppender sql = new StringAppender(200);
		
		if (from.getJoins().isEmpty()) {
			// invalid SQL
			throw new IllegalArgumentException("Empty from");
		} else {
			// on ajoute le point de départ des jointures: la première table
			Join headJoin = Iterables.first(from.getJoins());
			Table firstTable = headJoin.getLeftTable().getTable();
			cat(firstTable, sql);
			// la suite ne traite que les clauses de jointure
			Join previousJoin = null;	// nécessaire pour déterminer la prochaine table de jointure
			for (Join join : from) {
				Table previousTable = previousJoin == null ? join.getRightTable().getTable() : diff(previousJoin, join);
				if (join instanceof RawTableJoin) {
					cat(previousTable, join.getJoinDirection(), ((RawTableJoin) join).getJoinClause(), sql);
				} else if (join instanceof ColumnJoin) {
					String joinClause = getName(((ColumnJoin) join).getLeftColumn()) + " = " + getName(((ColumnJoin) join).getRightColumn());
					cat(previousTable, join.getJoinDirection(), joinClause, sql);
				}
				previousJoin = join;
			}
			
		}
		
		return sql.toString();
	}

	/**
	 * Renvoie la table de join2 absente de join1
	 * @param join1
	 * @param join2
	 * @return la table de join2 absente de join1, lève une exception si toutes les tables de join2 sont dans join1
	 */
	private Table diff(Join join1, Join join2) {
		Set<AliasedTable> tables1 = new TreeSet<>(diffAliasedTableComparator);
		tables1.add(join1.getLeftTable());
		tables1.add(join1.getRightTable());
		Set<AliasedTable> tables2 = new TreeSet<>(diffAliasedTableComparator);
		tables2.add(join2.getLeftTable());
		tables2.add(join2.getRightTable());
		tables2.removeAll(tables1);
		Iterator<AliasedTable> tables2Iterator = tables2.iterator();
		if (tables2Iterator.hasNext()) {
			return tables2Iterator.next().getTable();
		} else {
			throw new IllegalArgumentException("Second join doesn't join on new table");
		}
	}

	/**
	 * Comparateur de AliasedTable sur le nom et alias de la table
	 */
	private class DiffAliasedTableComparator implements Comparator<AliasedTable> {
		
		@Override
		public int compare(AliasedTable t1, AliasedTable t2) {
			// NB: il faut comparer les tables avec leur alias, mais pas celui de AliasedTable car ici il peut ne pas
			// avoir d'alias (selon la méthode de joiture appelée), c'est celui du From qui fait foi
			return (t1.getTable().getName()+getAlias(t1.getTable()))
					.compareToIgnoreCase(t2.getTable().getName()+getAlias(t2.getTable()));
		}
	}

	protected void cat(Table rightTable, Boolean joinDirection, String joinClause, StringAppender sql) {
		String joinType;
		if (joinDirection == null) {
			joinType = INNER_JOIN;
		} else if (!joinDirection) {
			joinType = LEFT_OUTER_JOIN;
		} else {
			joinType = RIGHT_OUTER_JOIN;
		}
		sql.cat(joinType);
		cat(rightTable, sql);
		sql.cat(" on ", joinClause);
	}

	private void cat(Table leftTable, StringAppender sql) {
		String tableAlias = getAlias(leftTable);
		sql.cat(leftTable.getName()).catIf(!Strings.isEmpty(tableAlias), " as ", tableAlias);
	}

}