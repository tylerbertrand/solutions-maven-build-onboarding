/*
 * Copyright 2011-2013 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cassandra.core.keyspace;

import static org.springframework.cassandra.core.cql.CqlStringUtils.checkIdentifier;
import static org.springframework.cassandra.core.cql.CqlStringUtils.identifize;

/**
 * Base class for column change specifications.
 * 
 * @author Matthew T. Adams
 */
public abstract class ColumnChangeSpecification {

	private String name;

	public ColumnChangeSpecification(String name) {
		setName(name);
	}

	private void setName(String name) {
		checkIdentifier(name);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getNameAsIdentifier() {
		return identifize(name);
	}
}
