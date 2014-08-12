package com.example.grouponlikepushnotification;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import com.groboot.pushapps.PushAppsMessageInterface;
import com.groboot.pushapps.PushAppsRegistrationInterface;
import com.groboot.pushapps.PushManager;
import com.groboot.pushapps.v4.NotificationCompat;

public class DemoApplication extends Application {
	public static final String GOOGLE_API_PROJECT_NUMBER = "XXXXXXXXXX"; // your
																			// sender
																			// id
																			// (google
																			// API
																			// project
																			// id)
	public static final String PUSHAPPS_APP_TOKEN = "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY"; // your
																							// application
																							// token
																							// from
																							// PushApps

	@Override
	public void onCreate() {
		super.onCreate();

		// first we initialize the push manager, you can also initialize the
		// PushManager in your main activity.
		PushManager.init(getBaseContext(), GOOGLE_API_PROJECT_NUMBER,
				PUSHAPPS_APP_TOKEN);
		PushManager.getInstance(getApplicationContext())
				.setShouldStartIntentAsNewTask(false);
		// these methods are both optional and used for the notification
		// customization
		PushManager.getInstance(getApplicationContext())
				.setShouldStackNotifications(true);

		// register for message events
		PushManager.getInstance(getApplicationContext())
				.registerForMessagesEvents(new PushAppsMessageInterface() {

					// This method will get called every time the device will receive a push message
					@Override
					public void onMessage(Context ctx, Intent intent) {
						
						String titleTxt = intent.getStringExtra(PushManager.NOTIFICATION_TITLE_KEY); // Get the title string from the
																									// push notification message
						String subTitleTxt = intent.getStringExtra(PushManager.NOTIFICATION_MESSAGE_KEY); // Get the message string from the
																										// push notification message
						String extraData = intent.getStringExtra("info"); // Your Custom JSON key
						String actionButton = "Click Me!"; // Some default string
						String imageUrl = "";
						try {
							JSONObject jsonObject = new JSONObject(extraData);
							actionButton = jsonObject.getString("button_text"); // Extract the text for our action button,
																				// from the custom JSON
							imageUrl = jsonObject.getString("picture_url");
						} catch (JSONException e) {}
						
						// The intent to start, when the user clicks the notification
						Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
						TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
						stackBuilder.addParentStack(MainActivity.class);
						stackBuilder.addNextIntent(resultIntent);
						PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
						
						// The custom view
						RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.custom_notification);
						expandedView.setTextViewText(R.id.notification_title, titleTxt);
						expandedView.setTextViewText(R.id.notification_subtitle, subTitleTxt);
						expandedView.setTextViewText(R.id.notification_colored_text, actionButton);
						expandedView.setImageViewBitmap(R.id.notification_main_image_view, drawableFromUrl(imageUrl));
						
						// Building the notification that will show up in the notification center
						Notification notification = new NotificationCompat.Builder(
								getApplicationContext())
								.setSmallIcon(R.drawable.small_pic)
								.setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
								.setDefaults(Notification.DEFAULT_VIBRATE)
								.setAutoCancel(true)
								.setContentIntent(resultPendingIntent)
								.setContentTitle(titleTxt)
								.setContentText(subTitleTxt)
								.build();

						notification.bigContentView = expandedView;

						NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						mNotificationManager.notify(1, notification);

					}
				});

		// Get notified on registration events
		PushManager.getInstance(getApplicationContext())
				.registerForRegistrationEvents(
						new PushAppsRegistrationInterface() {

							@Override
							public void onUnregistered(Context arg0, String arg1) {
								Log.d("PushAppsDemo",
										"Unregistered successfully");

							}

							@Override
							public void onRegistered(Context arg0, String arg1) {
								Log.d("PushAppsDemo",
										"Registered succeeded. Your registration ID: "
												+ arg1);
							}

							@Override
							public void onError(Context paramContext,
									String errorMessage) {
								Log.d("PushAppsDemo",
										"We encountered some error during the registration process: "
												+ errorMessage);
							}
						});
	}
	
	public static Bitmap drawableFromUrl(String url) {
	    Bitmap x = null;

	    HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) new URL(url).openConnection();
			connection.connect();
		    InputStream input = connection.getInputStream();
		    x = BitmapFactory.decodeStream(input);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    return x;
	}
}
