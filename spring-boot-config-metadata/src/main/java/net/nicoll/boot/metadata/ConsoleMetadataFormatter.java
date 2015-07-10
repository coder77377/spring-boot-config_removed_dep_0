/*
 * Copyright 2012-2015 the original author or authors.
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

package net.nicoll.boot.metadata;

import java.text.BreakIterator;
import java.util.List;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataSource;
import org.springframework.util.StringUtils;

/**
 *
 * @author Stephane Nicoll
 */
public class ConsoleMetadataFormatter extends AbstractMetadataFormatter implements MetadataFormatter {

	@Override
	public String formatMetadata(ConfigurationMetadataRepository repository) {
		StringBuilder out = new StringBuilder();
		List<ConfigurationMetadataGroup> groups = sortGroups(repository.getAllGroups().values());
		for (ConfigurationMetadataGroup group : groups) {
			out.append("========================================").append(NEW_LINE);
			StringBuilder sb = new StringBuilder();
			for (ConfigurationMetadataSource source : group.getSources().values()) {
				sb.append(source.getType()).append(" ");
			}
			out.append("Group --- ").append(group.getId()).append("(").append(sb.toString().trim()).append(")")
					.append(NEW_LINE).append("========================================").append(NEW_LINE);
			List<ConfigurationMetadataProperty> properties = sortProperties(group.getProperties().values());
			for (ConfigurationMetadataProperty property : properties) {
				out.append(formatProperty(property)).append(NEW_LINE);
			}
		}
		return out.toString();
	}

	public static String formatProperty(ConfigurationMetadataProperty property) {
		StringBuilder item = new StringBuilder(property.getId()).append("=");
		Object defaultValue = property.getDefaultValue();
		if (defaultValue != null) {
			item.append(defaultValueToString(defaultValue));
		}
		item.append(" # (").append(property.getType()).append(")");
		String description = property.getDescription();
		if (StringUtils.hasText(description)) {
			item.append(" - ").append(descriptionToTagLine(description));
		}
		else {
			item.append(" --- NO DESCRIPTION");
		}
		return item.toString();
	}

	public static String defaultValueToString(Object defaultValue) {
		if (defaultValue instanceof Object[]) {
			return StringUtils.arrayToCommaDelimitedString((Object[]) defaultValue);
		}
		else {
			return defaultValue.toString();
		}
	}

	public static String descriptionToTagLine(String description) {
		int dot = description.indexOf(".");
		if (dot != -1) {
			BreakIterator breakIterator = BreakIterator.getSentenceInstance();
			breakIterator.setText(description);
			return description.substring(breakIterator.first(), breakIterator.next()).trim();
		}
		else {
			return description;
		}
	}

}
