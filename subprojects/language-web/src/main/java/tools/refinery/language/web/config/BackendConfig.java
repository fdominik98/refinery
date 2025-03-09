/*
 * SPDX-FileCopyrightText: 2021-2023 The Refinery Authors <https://refinery.tools/>
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package tools.refinery.language.web.config;

import com.google.gson.annotations.SerializedName;

public class BackendConfig {
	private String apiBase;

	@SerializedName("webSocketURL")
	private String webSocketUrl;

	public BackendConfig(String apiBase, String webSocketUrl) {
		this.apiBase = apiBase;
		this.webSocketUrl = webSocketUrl;
	}

	public String getWebSocketUrl() {
		return webSocketUrl;
	}

	public void setWebSocketUrl(String webSocketUrl) {
		this.webSocketUrl = webSocketUrl;
	}

	public String getApiBase() {
		return apiBase;
	}

	public void setApiBase(String apiBase) {
		this.apiBase = apiBase;
	}
}
