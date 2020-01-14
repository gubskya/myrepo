package org.acme.data;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@RegisterForReflection
public class Price
{
	private String product;
	private double value;
}
