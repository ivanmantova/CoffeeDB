package coffeedb;

import java.util.List;

import coffeedb.parser.Parser;

public class CoffeeDB {
	static CoffeeDB _singleton = null;
	private Config _config = null;
	private Catalog _catalog;
	private ExecutionEngine _engine;
	private Logger _logger;
	
	private CoffeeDB() {
		init();
	}
	
	private void init() {
		_engine = new ExecutionEngine();
		_catalog = new Catalog();
		_logger = new Logger();
	}
	
	private void shutdown() {
		_engine.shutdown();
	}
	
	public void reset() {
		_catalog.clean();
	}

	public void test() {
		Catalog catalog = CoffeeDB.getInstance().getCatalog();
		Table table = new Table("TestTable", null);
		catalog.addTable(table);
	}
	
	private void printResults(Transaction transaction) {
		assert (transaction.didCommit());
		List<Tuple> results = transaction.getResult();
		for (Tuple tuple : results) {
			System.out.println(tuple);
		}
		
		if (results.size() == 0) {
			System.out.println("No rows selected");
		}
	}
	
	public void snapshot() {
		getLogger().snapshot(this);
	}
	
	public void recoverFromLog() {
		getLogger().recoverFromSnapshot(this);
	}
	
	public List<Tuple> runQuery(String query) {
		Parser parser = new Parser();
		QueryPlan plan = parser.parseQuery(query);
		
		Transaction transaction = _engine.executeQueryPlan(plan);
		assert (transaction.didCommit());
		printResults(transaction);
		return transaction.getResult();
		/*
		while (!transaction.didCommit()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		printResults(transaction);
		
		/*
		QueryOptimizer optimizer = new QueryOptimizer();
		optimizer.optimizePlan(queryPlan);
		
		ExecutionEngine engine = new ExecutionEngine();
		engine.runPlan(queryPlan);
		*/
	}
	
	public static Catalog catalog() {
		return getInstance().getCatalog();
	}
	
	public static Logger logger() {
		return getInstance().getLogger();
	}
	
	public static CoffeeDB getInstance() {
		if (_singleton == null) {
			_singleton = new CoffeeDB();
		}
		
		return _singleton;
	}
	
	public Catalog getCatalog() {
		return _catalog;
	}
	
	public Logger getLogger() {
		return _logger;
	}
	
	public void setConfig(Config config) {
		_config = config;
	}
	
	public Config getConfig() {
		assert (_config != null);
		return _config;
	}
	
	public static Config parseConfig(String[] args) {
		return new Config();
	}
	
	public static void usage() {
	}
	
	public static void main(String[] args) 
		throws InterruptedException {
		Config config = parseConfig(args);
		CoffeeDB database = CoffeeDB.getInstance();
		database.setConfig(config);
		
		database.runQuery("create table test (a int, b int);");
		database.runQuery("insert into test values (10, 20);");
		database.runQuery("insert into test values (25, 20);");
		database.runQuery("insert into test values (40, 10);");
		database.runQuery("select * from test;");
		System.out.println("Running aggregate");
		database.runQuery("select min(a) from test;");
		/*
		System.out.println("Run sum with group");
		database.runQuery("select sum(a) from test group by b;");
		System.out.println("Running min");
		database.runQuery("select min(a) from test group by b;");
		
		System.out.println("Running count");
		database.runQuery("select count(a) from test group by b;");
		
		System.out.println("Running max");
		database.runQuery("select max(a) from test group by b;");
		
		System.out.println("Running avg");
		database.runQuery("select avg(a) from test group by b;");
		*/
		database.shutdown();
	}

}
