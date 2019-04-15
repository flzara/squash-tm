/**
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.squashtest.tm.api.security.authentication;

import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * This is a custom OnPropertyCondition class to replace {@link org.springframework.boot.autoconfigure.condition.OnPropertyCondition} and used
 * in {@link ConditionalOnAuthProviderProperty}.
 * instead of the original havingValue() check, here we check if lists or arrays contains a specific value
 *
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 40)
public class OnAuthProviderPropertyCondition extends SpringBootCondition {

	private static final String AUTH_PROVIDER = "authentication.provider";

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		List<AnnotationAttributes> allAnnotationAttributes =
			annotationAttributesFromMultiValueMap(metadata.getAllAnnotationAttributes(ConditionalOnAuthProviderProperty.class.getName()));
		List<ConditionMessage> noMatch = new ArrayList<>();
		List<ConditionMessage> match = new ArrayList<>();
		for (AnnotationAttributes annotationAttributes : allAnnotationAttributes) {
			ConditionOutcome outcome = determineOutcome(annotationAttributes, context.getEnvironment());
			(outcome.isMatch() ? match : noMatch).add(outcome.getConditionMessage());
		}
		if (!noMatch.isEmpty()) {
			return ConditionOutcome.noMatch(ConditionMessage.of(noMatch));
		}
		return ConditionOutcome.match(ConditionMessage.of(match));
	}

	private List<AnnotationAttributes> annotationAttributesFromMultiValueMap(MultiValueMap<String, Object> multiValueMap) {
		List<Map<String, Object>> maps = new ArrayList<>();
		for (Map.Entry<String, List<Object>> entry : multiValueMap.entrySet()) {
			for (int i = 0; i < entry.getValue().size(); i++) {
				Map<String, Object> map;
				if (i < maps.size()) {
					map = maps.get(i);
				}
				else {
					map = new HashMap<>();
					maps.add(map);
				}
				map.put(entry.getKey(), entry.getValue().get(i));
			}
		}
		List<AnnotationAttributes> annotationAttributes = new ArrayList<>(maps.size());
		for (Map<String, Object> map : maps) {
			annotationAttributes.add(AnnotationAttributes.fromMap(map));
		}
		return annotationAttributes;
	}

	private ConditionOutcome determineOutcome(AnnotationAttributes annotationAttributes,
											  PropertyResolver resolver) {
		OnAuthProviderPropertyCondition.Spec spec = new OnAuthProviderPropertyCondition.Spec(annotationAttributes);
		List<String> missingProperties = new ArrayList<>();
		List<String> nonMatchingProperties = new ArrayList<>();
		spec.collectProperties(resolver, missingProperties, nonMatchingProperties);
		if (!missingProperties.isEmpty()) {
			return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnAuthProviderProperty.class, spec)
				.didNotFind("property", "properties")
				.items(ConditionMessage.Style.QUOTE, missingProperties));
		}
		if (!nonMatchingProperties.isEmpty()) {
			return ConditionOutcome.noMatch(ConditionMessage.forCondition(ConditionalOnAuthProviderProperty.class, spec)
				.found("different value in property", "different value in properties")
				.items(ConditionMessage.Style.QUOTE, nonMatchingProperties));
		}
		return ConditionOutcome.match(ConditionMessage.forCondition(ConditionalOnAuthProviderProperty.class, spec)
			.because("matched"));
	}

	private static class Spec {

		private final String value;

		private final boolean matchIfMissing;

		Spec(AnnotationAttributes annotationAttributes) {

			this.value = annotationAttributes.getString("value");
			this.matchIfMissing = annotationAttributes.getBoolean("matchIfMissing");
		}

		private void collectProperties(PropertyResolver resolver, List<String> missing,
									   List<String> nonMatching) {
			if (resolver.containsProperty(AUTH_PROVIDER)) {
				if (!isMatch(resolver.getProperty(AUTH_PROVIDER, String[].class), this.value)) {
					nonMatching.add(AUTH_PROVIDER);
				}
			}
			else {
				if (!this.matchIfMissing) {
					missing.add(AUTH_PROVIDER);
				}
			}
		}

		private boolean isMatch(String[] values, String requiredValue) {
			if (StringUtils.hasLength(requiredValue)) {
				return Arrays.asList(values).contains(requiredValue);
			}
			return !Arrays.asList(values).contains("false");
		}

	}
}
