/**
 * Copyright (C) 2010-2014 Morgner UG (haftungsbeschränkt)
 *
 * This file is part of Structr <http://structr.org>.
 *
 * Structr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Structr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Structr.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.structr.schema.parser;

import org.apache.commons.lang.StringUtils;
import org.structr.common.error.ErrorBuffer;
import org.structr.common.error.InvalidPropertySchemaToken;
import org.structr.core.entity.SchemaNode;
import org.structr.core.property.DoubleProperty;
import org.structr.schema.SchemaHelper.Type;

/**
 *
 * @author Christian Morgner
 */
public class DoublePropertyParser extends NumericalPropertyParser {
	
	public DoublePropertyParser(final ErrorBuffer errorBuffer, final String className, final String propertyName, final String dbName, final String rawSource, final String defaultValue) {
		super(errorBuffer, className, propertyName, dbName, rawSource, defaultValue);
	}

	@Override
	public String getPropertyType() {
		return DoubleProperty.class.getSimpleName();
	}

	@Override
	public String getValueType() {
		return Double.class.getSimpleName();
	}
	
	@Override
	public Type getKey() {
		return Type.Double;
	}

	@Override
	public Number parseNumber(final ErrorBuffer errorBuffer, final String source, final String which) {

		try {
			return Double.parseDouble(source);
			
		} catch (Throwable t) {
			
			errorBuffer.add(SchemaNode.class.getSimpleName(), new InvalidPropertySchemaToken(source, "invalid_" + which +"_bound", StringUtils.capitalize(which) + " bound must be of type Double."));
		}
		
		return null;
	}

	@Override
	public String getDefaultValueSource() {
		return defaultValue.concat("d");
	}
}
