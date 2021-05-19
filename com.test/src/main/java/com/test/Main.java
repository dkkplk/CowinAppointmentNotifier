package com.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

public class Main {
	public static final DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	public static final String  RINGTONE = "C:\\Users\\dkumar\\Documents\\cowinhelp\\com.test\\src\\main\\resources\\preview.wav";

	public static void playSound() throws Exception {
		File audioFile = new File(RINGTONE);
		AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
		AudioFormat format = audioStream.getFormat();
		DataLine.Info info = new DataLine.Info(Clip.class, format);
		Clip audioClip = (Clip) AudioSystem.getLine(info);
		audioClip.open(audioStream);
		audioClip.start();
		Thread.sleep(500);
		while (audioClip.isActive()) {
		}
		audioClip.close();
		audioStream.close();
	}

	public static void main(String[] args) throws Exception {
		OkHttpClient client = new OkHttpClient();
		LocalDate date = LocalDate.now().plusDays(1);
		String formattedDate = date.format(format);
		while (true) {
			Request request = new Request.Builder().url(
					"https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode=344022&date=" + formattedDate)
					.header("User-Agent", "PostmanRuntime/7.28.0").addHeader("Accept", "*/*")
					.addHeader("Accept-Encoding", "gzip, deflate, br").addHeader("Connection", "keep-alive").build();
			Response response = client.newCall(request).execute();
			int code = response.code();
			if (code != 200) {
				System.out.println(code);
			}
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.body().byteStream()));
			String next = null;
			StringBuilder builder = new StringBuilder();
			while ((next = bufferedReader.readLine()) != null) {
				builder.append(next);
			}
			bufferedReader.close();

			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(builder.toString());
			JsonNode centersArray = actualObj.elements().next();
			Iterator<JsonNode> centers = centersArray.elements();
			while (centers.hasNext()) {
				JsonNode center = centers.next();
				JsonNode sessions = center.get("sessions");
				Iterator<JsonNode> sessionsItr = sessions.elements();
				while (sessionsItr.hasNext()) {
					JsonNode session = (JsonNode) sessionsItr.next();
					int ageLimit = Integer.parseInt(session.get("min_age_limit").toString());
					int available = Integer.parseInt(session.get("available_capacity").toString());
					if (ageLimit == 18  && available > 0) {
						System.out.println("Center: " + center.get("name"));
						System.out.println("Date: " + session.get("date"));
						System.out.println("Age: " + ageLimit + "+");
						System.out.println("Available: " + available);
						System.out.println("-----------------------------------------------------------");
						playSound();
					}
				}
			}
			Thread.sleep(3000);
		}
	}
}
