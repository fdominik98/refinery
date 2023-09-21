/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */

plugins {
	id("tools.refinery.gradle.java-library")
}

dependencies {
	implementation("com.google.guava:guava:30.1-jre")
	api(project(":refinery-store-query"))
	api(project(":refinery-store-query-viatra"))
    implementation(project(mapOf("path" to ":refinery-store-dse")))
	implementation(project(mapOf("path" to ":refinery-store-dse")))
	implementation(project(mapOf("path" to ":refinery-store-dse")))
	testImplementation(project(mapOf("path" to ":refinery-store-dse")))
	testImplementation(project(mapOf("path" to ":refinery-store-dse")))
}
