package cs247.group15.app;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jdom.JDOMException;

import cs247.group15.data.Constants;
import cs247.group15.data.DataRequest;
import cs247.group15.data.ImportantInformation;
import cs247.group15.data.ListClass;
import cs247.group15.data.PrintableDate;
import cs247.group15.data.Properties;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.util.Log;

public class CS247Service extends IntentService {

	public CS247Service() {
		super("CS247Service");
	}

	ServiceBinder binder;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(Constants.information, "The service has been started.");
		binder = new ServiceBinder();
		binder.startAutoUpdater();
		//binder.sendRequest(Properties.getLastUpdate());
		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {}
	
	@Override
	public Binder onBind(Intent intent) {
		super.onBind(intent);
		return binder;
	}
	
	class ServiceBinder extends Binder
	{
		private List<ImportantInformation> listOfInformation = new ArrayList<ImportantInformation>();
		private Timer timer;
		
		public CS247Service getService() {return CS247Service.this;}
		
		public List<ImportantInformation> getImportantInformationList()
		{   
			return listOfInformation;
		}
		public ArrayList<ListClass> getDatedImportantInformationList()
		{
			ArrayList<ListClass> datedList = new ArrayList<ListClass>();
			
			if(listOfInformation.size()>0)
			{
				PrintableDate currentDate = listOfInformation.get(0).getDate();
				datedList.add(currentDate);
				
				for(ImportantInformation info : listOfInformation)
				{
					if(info.getDate().sameDayAs(currentDate))
					{
						datedList.add(info);
					}
					else
					{
						datedList.add(info.getDate());
						datedList.add(info);
						currentDate = info.getDate();
					}
				}
			}
			return datedList;
		}
		
		public void sendRequest(long date, OnRequestComplete listener)
		{
			/*DataSender ds = null;
			try {
				Log.d(Constants.information, "Sending data request with date: " + date);
				ds = new DataSender(new DataRequest(date));
				Properties.setLastUpdate(System.currentTimeMillis()); //use date of last item in returned list!
				//TODO: might want to put a "correction constant" in here to allow for delay between updates being available & getting the updates
			} 
			catch (UnknownHostException e) {
				Log.d(Constants.error, e.getMessage());
				e.printStackTrace();
			} 
			catch (IOException e) {
				Log.d(Constants.error, e.getMessage());
				e.printStackTrace();
			} 
			catch (JDOMException e) {
				Log.d(Constants.error, e.getMessage());
				e.printStackTrace();
			}
			
			if(ds!=null)
			{
				Log.d(Constants.information, ds.getList().toString());
				listOfInformation = ds.getList();
			}*/
			
	        //Test data
			listOfInformation.clear(); //usually would just check for "outofdate" informations & add the new ones to the list
			ArrayList<String> sources = new ArrayList<String>();
			sources.add("this is a source");
			sources.add("this is another source");
			listOfInformation.add(new ImportantInformation("News1", 8, new Date(54546), "this is inference", sources, "this is other"));
			listOfInformation.add(new ImportantInformation("News2", new Date(54547)));
			listOfInformation.add(new ImportantInformation("News1", new Date(234567781)));
			listOfInformation.add(new ImportantInformation("News2", new Date(234567791)));
			listOfInformation.add(new ImportantInformation("News3", new Date(234567801)));
			listOfInformation.add(new ImportantInformation("News1", new Date(456789678)));
	        listOfInformation.add(new ImportantInformation("News2", new Date(456789679)));
	        listOfInformation.add(new ImportantInformation("News3", new Date(458678968)));
	        listOfInformation.add(new ImportantInformation("News4", new Date(458678968)));
	        listOfInformation.add(new ImportantInformation("News5", new Date(458678968)));
	        listOfInformation.add(new ImportantInformation("News6", new Date(458678969)));
	        listener.onSuccess();
	        checkNotifications(listOfInformation); //put the list of new infos into here
		}
		
		private void startAutoUpdater()
		{
			timer = new Timer();
			timer.schedule(new TimerTask(){

				@Override
				public void run() {
					sendRequest(Properties.getLastUpdate(), new OnRequestComplete() {
						
						public void onSuccess() {
							//Set last updated time to the time associated with the last ImportantInformation received
							Properties.setLastUpdate(
									listOfInformation.get(listOfInformation.size()-1).getDate().getTime()
							);
						}
						
						public void onFail() {}
					});
				}}, new Date(System.currentTimeMillis()),
			Properties.getUpdateFrequency());
		}
		
		private void checkNotifications(List<ImportantInformation> newList)
		{
			NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
			for(int i = 0; i<newList.size(); i++)
			{
				ImportantInformation info = newList.get(i);
				if(info.getImportance()>=Properties.getImportanceLevel())
				{
					Notification not = new Notification(R.drawable.ic_launcher, info.toString(), System.currentTimeMillis());
					
					not.defaults |= Notification.DEFAULT_VIBRATE;
					not.flags |= Notification.FLAG_AUTO_CANCEL;
					
					Intent notificationIntent = new Intent(getApplicationContext(), DrillDownInformation.class);
					notificationIntent.putExtra(Constants.ImportantInformationTag, info);
					
					PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
					not.setLatestEventInfo(getApplicationContext(), "Important", info.toString(), contentIntent);
					
					notificationManager.notify(i, not);
				}
			}
		}
	}

}