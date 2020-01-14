package org.acme.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.vertx.axle.mysqlclient.MySQLPool;
import io.vertx.axle.sqlclient.Row;
import io.vertx.axle.sqlclient.RowSet;
import io.vertx.axle.sqlclient.Tuple;
import org.acme.data.Price;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PriceDao
{
	@Inject
	@ConfigProperty(name = "db.schema.create", defaultValue = "true")
	boolean schemaCreate;

	@Inject
	MySQLPool client;

	@PostConstruct
	void config() {
		if (schemaCreate) {
			initdb();
		}
	}

	private void initdb() {
		client.query("DROP TABLE IF EXISTS prices")
			  .thenCompose(r -> client.query("CREATE TABLE prices (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
											 + "product VARCHAR(45) NOT NULL, "
											 + "value DOUBLE NOT NULL,"
											 + "PRIMARY KEY (`id`),"
											 + "INDEX(product))"))
			  .thenCompose(r -> client.query("INSERT INTO prices (product, value) VALUES ('prod1',99)"))
			  .thenCompose(r -> client.query("INSERT INTO prices (product, value) VALUES ('prod2',100)"))
			  .thenCompose(r -> client.query("INSERT INTO prices (product, value) VALUES ('prod3',101)"))
			  .thenCompose(r -> client.query("INSERT INTO prices (product, value) VALUES ('prod4',102)"))
			  .toCompletableFuture()
			  .join();
	}

	public CompletionStage<List<Price>> findAll() {
		return client.query("SELECT product, value FROM prices").thenApply(prRowSet -> {
			List<Price> list = new ArrayList<>(prRowSet.size());
			for (Row row : prRowSet) {
				list.add(from(row));
			}
			return list;
		});
	}

	public CompletionStage<List<Price>> findByProduct(String product)
	{
		return client.preparedQuery("SELECT product, value FROM prices WHERE product = ?", Tuple.of(product))
					 .thenApply(RowSet::iterator)
					 .thenApply(iterator -> {
						 List<Price> list = new ArrayList<>();
					 	while (iterator.hasNext())
						{
							Row row = iterator.next();
							list.add(from(row));
						}
					 	return list;
					 });
	}

	private static Price from(Row row) {
		return new Price(row.getString("product"), row.getDouble("value"));
	}
}
