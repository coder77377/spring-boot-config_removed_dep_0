/*
 * Copyright 2012-2023 the original author or authors.
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

package net.nicoll.boot.config.diff;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.nicoll.boot.config.loader.AetherDependencyResolver;
import net.nicoll.boot.config.loader.ConfigurationMetadataLoader;
import net.nicoll.boot.metadata.MetadataUtils;

import org.springframework.boot.configurationmetadata.ConfigurationMetadataGroup;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataProperty;
import org.springframework.boot.configurationmetadata.ConfigurationMetadataRepository;
import org.springframework.util.StringUtils;

public class AppendixGenerator {

	static final String NEW_LINE = System.getProperty("line.separator");

	public static void main(String[] args) throws Exception {
		ConfigurationMetadataLoader loader = new ConfigurationMetadataLoader(
				AetherDependencyResolver.withAllRepositories());
		ConfigurationMetadataRepository repo = loader.loadRepository("2.1.0.BUILD-SNAPSHOT");
		attachRootPropertyToGroup(repo);

		List<ConfigurationMetadataGroup> groups = MetadataUtils.sortGroups(repo.getAllGroups().values());
		StringBuilder sb = new StringBuilder();
		for (ConfigurationMetadataGroup group : groups) {
			sb.append("# ").append(group.getId()).append(NEW_LINE);
			List<ConfigurationMetadataProperty> properties = MetadataUtils
				.sortProperties(group.getProperties().values())
				.stream()
				.filter(p -> !p.isDeprecated())
				.toList();
			for (ConfigurationMetadataProperty property : properties) {
				sb.append(property.getId()).append("=");
				if (property.getDefaultValue() != null) {
					sb.append(defaultValueToString(property.getDefaultValue()));
				}
				sb.append(" # ").append(cleanDescription(property.getDescription())).append(NEW_LINE);
			}
			sb.append(NEW_LINE);
		}

		System.out.println(sb);

	}

	/**
	 * Attempt to attach a property from the root group to an existing group.
	 * @param repository the metadata repository to use
	 */
	private static void attachRootPropertyToGroup(ConfigurationMetadataRepository repository) {
		ConfigurationMetadataGroup rootGroup = repository.getAllGroups()
			.get(ConfigurationMetadataRepository.ROOT_GROUP);
		Iterator<Map.Entry<String, ConfigurationMetadataProperty>> it = rootGroup.getProperties().entrySet().iterator();
		while (it.hasNext()) {
			ConfigurationMetadataProperty property = it.next().getValue();
			String id = property.getId();
			int lastdot = id.lastIndexOf('.');
			if (lastdot != -1) {
				String groupId = id.substring(0, lastdot);
				ConfigurationMetadataGroup group = repository.getAllGroups().get(groupId);
				if (group != null) {
					System.out.println("Please consider moving property " + id + " to group " + "" + groupId
							+ " (currently on the root group).");
					group.getProperties().put(id, property);
					it.remove();
				}
			}
		}

	}

	private static String defaultValueToString(Object defaultValue) {
		if (defaultValue instanceof Object[]) {
			return StringUtils.arrayToCommaDelimitedString((Object[]) defaultValue);
		}
		else {
			return defaultValue.toString();
		}
	}

	private static String cleanDescription(String description) {
		if (description == null) {
			return "";
		}
		description = Character.toUpperCase(description.charAt(0)) + description.substring(1);
		if (!description.endsWith(".")) {
			description += ".";
		}
		return description;
	}

}
