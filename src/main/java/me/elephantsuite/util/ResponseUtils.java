package me.elephantsuite.util;

import com.google.gson.JsonObject;

public class ResponseUtils {


	public static boolean isFailure(JsonObject response) {
		return !response.get("status").getAsString().equals("SUCCESS");
	}

	public static String getMessage(JsonObject response) {
		return response.get("message").getAsString();
	}
}
