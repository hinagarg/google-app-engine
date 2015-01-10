import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.tools.cloudstorage.GcsFileOptions;
import com.google.appengine.tools.cloudstorage.GcsFilename;
import com.google.appengine.tools.cloudstorage.GcsInputChannel;
import com.google.appengine.tools.cloudstorage.GcsOutputChannel;
import com.google.appengine.tools.cloudstorage.GcsService;
import com.google.appengine.tools.cloudstorage.GcsServiceFactory;
import com.google.appengine.tools.cloudstorage.RetryParams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import com.google.apphosting.api.ApiProxy;
import java.util.concurrent.ThreadFactory;
import com.google.appengine.api.ThreadManager;
import java.util.List;
import java.util.ArrayList;



@SuppressWarnings("serial")
public class UploadDownloadServlet extends HttpServlet {
																																					

	public void init() {	 
		ServletContext sc = getServletContext();	 
		List timerList = new ArrayList();
		timerList.add(0l);
		sc.setAttribute("totalUploadTime",timerList);
		sc.setAttribute("refreshCount",1);
    }
	
	private final GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
		.initialRetryDelayMillis(10)
		.retryMaxAttempts(10)
		.totalRetryPeriodMillis(15000)
		.build());


	private static final int BUFFER_SIZE = 2 * 1024 * 1024;

		
	//For downloading an object(file)
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if(true){
			GcsFilename fileName = getFileName(req);
			GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, BUFFER_SIZE);
			copy(Channels.newInputStream(readChannel), resp.getOutputStream());
		}
		else{
			GcsFilename fileName = getFileName(req);
			GcsInputChannel readChannel = gcsService.openPrefetchingReadChannel(fileName, 0, BUFFER_SIZE);
			final InputStream input = Channels.newInputStream(readChannel);
			final OutputStream output = resp.getOutputStream();
			
			Thread thread = ThreadManager.createBackgroundThread(new Runnable() {
			@Override
			public void run(){
				try{
					UploadDownloadServlet.this.copyNew(input,output);
				}
				catch(IOException e){
		
				}
			}});			
			thread.start();
		}
	}

	//For uploading an object(file)
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		if(true){
			GcsOutputChannel outputChannel = gcsService.createOrReplace(getFileName(req), GcsFileOptions.getDefaultInstance());
			copy(req.getInputStream(), Channels.newOutputStream(outputChannel));
		}
		else{
			GcsOutputChannel outputChannel = gcsService.createOrReplace(getFileName(req), GcsFileOptions.getDefaultInstance());
			final InputStream input = req.getInputStream();
			final OutputStream output = Channels.newOutputStream(outputChannel);
		
			Thread thread = ThreadManager.createBackgroundThread(new Runnable() {
			@Override
			public void run(){
				try{
					UploadDownloadServlet.this.copyNew(input,output);
				}
				catch(IOException e){
		
				}
			}});			
			thread.start();
		}
	}

	private GcsFilename getFileName(HttpServletRequest req) {
		String[] splits = req.getRequestURI().split("/", 4);
		if (!splits[0].equals("") || !splits[1].equals("gcs")) {
			throw new IllegalArgumentException("The URL is not formed as expected. Expecting /gcs/<bucket>/<object>");
		}
		return new GcsFilename(splits[2], splits[3]);
	}

	// Transfer the data from the inputStream to the outputStream. Then close both streams.
	private void copy(InputStream input, OutputStream output) throws IOException {
		long startTime = 0l;
		try {
			startTime = System.currentTimeMillis();
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = input.read(buffer);
			while (bytesRead != -1) {
				output.write(buffer, 0, bytesRead);
				bytesRead = input.read(buffer);
			}
		} finally {
			input.close();
			output.flush();
			output.close();
			long endTime = System.currentTimeMillis();
			ServletContext sc = getServletContext();
			List timerList = (ArrayList) sc.getAttribute("totalUploadTime");							
			timerList.add((endTime - startTime));			
			sc.setAttribute("totalUploadTime",timerList);
			sc.setAttribute("refreshCount",1);			
		}
	}
	
	private void copyNew(InputStream input, OutputStream output) throws IOException {
		
		long startTime = 0l;
		try {	  
		    startTime = System.currentTimeMillis();
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead = input.read(buffer);
			while (bytesRead != -1) {			
				output.write(buffer, 0, bytesRead);			
				bytesRead = input.read(buffer);			
			}
		}
		finally {			
		    output.flush(); 
			input.close();
			output.close();
			long endTime = System.currentTimeMillis();
			ServletContext sc = getServletContext();
			List timerList = (ArrayList) sc.getAttribute("totalUploadTime");				
			timerList.add((endTime - startTime));			
			sc.setAttribute("totalUploadTime",timerList);
			sc.setAttribute("refreshCount",1);			
		}			
	}
}
