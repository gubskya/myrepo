package org.acme;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import org.acme.dao.PriceDao;
import org.acme.data.Price;


@Path("/hello")
public class Hello
{
	private final Jsonb jsonb;

	@Inject
	Vertx vertx;

	@Inject
	PriceDao priceDao;

	public Hello() {
		jsonb = JsonbBuilder.create(new JsonbConfig());
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{name}")
	public CompletionStage<List<String>> greeting(@PathParam("name") String name) {
		return priceDao.findAll().thenApply(priceList -> {
			List<String> result = new ArrayList<>(priceList.size());
			for(Price price : priceList){
				result.add(jsonb.toJson(price));
			}
			return result;
		});
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{name}/{id}")
	public CompletionStage<Response> getById(@PathParam("id") String productId){
		return priceDao.findByProduct(productId).thenApply(prices -> prices.get(0))
					   .thenApply(price -> new JsonObject().put("product", price.getProduct()).put("value", price.getValue()))
					   .thenApply(Response::ok)
					   .thenApply(Response.ResponseBuilder::build);
	}
}
