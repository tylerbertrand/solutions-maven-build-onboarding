/*
 * Copyright 2017-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.cassandra.support;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * Cassandra connection properties using {@code config/cassandra-connection.properties}. Properties are generated during
 * the build and can be override using system properties.
 *
 * @author Mark Paluch
 * @author John Blum
 */
@SuppressWarnings("unused")
public class CassandraConnectionProperties extends Properties {

	private final static List<WeakReference<CassandraConnectionProperties>> instances = new ArrayList<>();

	private final String resourceName;

	/**
	 * Construct a new instance of {@link CassandraConnectionProperties} using properties
	 * from {@code config/cassandra-connection.properties}.
	 */
	public CassandraConnectionProperties() {
		this("/config/cassandra-connection.properties");
	}

	private CassandraConnectionProperties(@NonNull String resourceName) {

		this.resourceName = resourceName;

		loadProperties();

		instances.add(new WeakReference<>(this));
	}

	public void update() {

		try {
			// Caution: Rewriting properties during initialization.
			File file = new File(getClass().getResource(this.resourceName).toURI());

			try (FileOutputStream out = new FileOutputStream(file)) {
				store(out, "");
			}

			reload();
		}
		catch (Exception cause) {
			cause.printStackTrace();
			throw new IllegalStateException(cause);
		}
	}

	private static void reload() {

		for (WeakReference<CassandraConnectionProperties> ref : instances) {

			CassandraConnectionProperties properties = ref.get();

			if (properties != null) {
				properties.loadProperties();
			}
		}
	}

	private void loadProperties() {
		loadProperties(this.resourceName);
	}

	private void loadProperties(String resourceName) {

		try (InputStream in = getClass().getResourceAsStream(resourceName)){
			if (in != null) {
				load(in);
			}
		}
		catch (Exception cause) {
			throw new RuntimeException(cause);
		}
	}

	@Override
	public String getProperty(String key) {

		String value = super.getProperty(key);

		if (value == null) {
			value = System.getProperty(key);
		}

		return value;
	}

	@Override
	public synchronized Object setProperty(String key, String value) {
		System.setProperty(key, value);
		return super.setProperty(key, value);
	}

	/**
	 * @return the Cassandra hostname
	 */
	public String getCassandraHost() {
		return getProperty("build.cassandra.host");
	}

	public void setCassandraHost(String host) {
		setProperty("build.cassandra.host", host);
	}

	/**
	 * @return the Cassandra port (native).
	 */
	public int getCassandraPort() {
		return getInt("build.cassandra.native_transport_port");
	}

	public void setCassandraPort(int port) {
		setProperty("build.cassandra.native_transport_port", "" + port);
	}

	/**
	 * @return the Cassandra RPC port
	 */
	public int getCassandraRpcPort() {
		return getInt("build.cassandra.rpc_port");
	}

	/**
	 * @return the Cassandra SSL Storage port
	 */
	public int getCassandraSslStoragePort() {
		return getInt("build.cassandra.ssl_storage_port");
	}

	/**
	 * @return the Cassandra Storage port
	 */
	public int getCassandraStoragePort() {
		return getInt("build.cassandra.storage_port");
	}

	/**
	 * @return the Cassandra type (Embedded or External)
	 */
	public CassandraType getCassandraType() {

		String cassandraType = getProperty("build.cassandra.mode");

		return CassandraType.TESTCONTAINERS.name().equalsIgnoreCase(cassandraType) ? CassandraType.TESTCONTAINERS
			: CassandraType.EXTERNAL.name().equalsIgnoreCase(cassandraType) ? CassandraType.EXTERNAL
			: CassandraType.EMBEDDED;
	}

	/**
	 * Retrieve a property and return its value as {@code int}.
	 *
	 * @param propertyName name of the property, must not be empty and not {@literal null}.
	 * @return the property value
	 */
	private int getInt(String propertyName) {
		return convert(propertyName, Integer.class, Integer::parseInt);
	}

	/**
	 * Retrieve a property and return its value as {@code long}.
	 *
	 * @param propertyName name of the property, must not be empty and not {@literal null}.
	 * @return the property value
	 */
	public long getLong(String propertyName) {
		return convert(propertyName, Long.class, Long::parseLong);
	}

	/**
	 * Retrieve a property and return its value as {@code boolean}.
	 *
	 * @param propertyName name of the property, must not be empty and not {@literal null}.
	 * @return the property value
	 */
	public boolean getBoolean(String propertyName) {
		return convert(propertyName, Boolean.class, Boolean::parseBoolean);
	}

	private <T> T convert(String propertyName, Class<T> type, Converter<String, T> converter) {

		Assert.hasText(propertyName, "PropertyName must not be empty");

		String propertyValue = getProperty(propertyName);
		try {
			return converter.convert(propertyValue);
		} catch (Exception cause) {

			String message = "%1$s: cannot parse value [%2$s] of property [%3$s] as a [%4$s]";

			throw new IllegalArgumentException(
					String.format(message, this.resourceName, propertyValue, propertyName, type.getSimpleName()), cause);
		}
	}

	public enum CassandraType {
		EMBEDDED, EXTERNAL, TESTCONTAINERS
	}

	/*
	 * Overridden to remove timestamp from properties file
	 */
	@Override
	public void store(OutputStream out, String comments)
			throws IOException
	{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(out));
		synchronized (this) {
			for (Map.Entry<Object, Object> e : entrySet()) {
				String key = (String)e.getKey();
				String val = (String)e.getValue();
				key = saveConvert(key, true, true);
				/* No need to escape embedded and trailing spaces for value, hence
				 * pass false to flag.
				 */
				val = saveConvert(val, false, true);
				bw.write(key + "=" + val);
				bw.newLine();
			}
		}
		bw.flush();
	}

	private String saveConvert(String theString,
							   boolean escapeSpace,
							   boolean escapeUnicode) {
		int len = theString.length();
		int bufLen = len * 2;
		if (bufLen < 0) {
			bufLen = Integer.MAX_VALUE;
		}
		StringBuilder outBuffer = new StringBuilder(bufLen);
		HexFormat hex = HexFormat.of().withUpperCase();
		for(int x=0; x<len; x++) {
			char aChar = theString.charAt(x);
			// Handle common case first, selecting largest block that
			// avoids the specials below
			if ((aChar > 61) && (aChar < 127)) {
				if (aChar == '\\') {
					outBuffer.append('\\'); outBuffer.append('\\');
					continue;
				}
				outBuffer.append(aChar);
				continue;
			}
			switch(aChar) {
				case ' ':
					if (x == 0 || escapeSpace)
						outBuffer.append('\\');
					outBuffer.append(' ');
					break;
				case '\t':outBuffer.append('\\'); outBuffer.append('t');
					break;
				case '\n':outBuffer.append('\\'); outBuffer.append('n');
					break;
				case '\r':outBuffer.append('\\'); outBuffer.append('r');
					break;
				case '\f':outBuffer.append('\\'); outBuffer.append('f');
					break;
				case '=': // Fall through
				case ':': // Fall through
				case '#': // Fall through
				case '!':
					outBuffer.append('\\'); outBuffer.append(aChar);
					break;
				default:
					if (((aChar < 0x0020) || (aChar > 0x007e)) & escapeUnicode ) {
						outBuffer.append("\\u");
						outBuffer.append(hex.toHexDigits(aChar));
					} else {
						outBuffer.append(aChar);
					}
			}
		}
		return outBuffer.toString();
	}
}
