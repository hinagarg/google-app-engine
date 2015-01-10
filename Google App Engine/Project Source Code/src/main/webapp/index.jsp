<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
    <head>
        <title>Distributed Storage System with Cache</title>
    </head>
    <body>


<%@ page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@ page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@ page import="com.google.appengine.api.blobstore.UploadOptions" %>

<%@ page import="javax.cache.Cache" %>
<%@ page import="javax.cache.CacheException" %>
<%@ page import="javax.cache.CacheFactory" %>
<%@ page import="javax.cache.CacheManager" %>
<%@ page import="java.util.Collections" %>

<%@ page import="com.google.appengine.tools.cloudstorage.GcsService" %>
<%@ page import="com.google.appengine.tools.cloudstorage.GcsServiceFactory" %>
<%@ page import="com.google.appengine.tools.cloudstorage.RetryParams" %>
<%@ page import="com.google.appengine.tools.cloudstorage.ListResult" %>
<%@ page import="com.google.appengine.tools.cloudstorage.ListItem" %>


	<h3>Please browse to select file(s) to upload to the distributed storage system:</h3>
	<b>Upload using Google Cloud Storage API - Files with size less than 100MB: </b>
        <form  enctype="text/plain" method="get" name="putFile" id="putFile">
          <div>
	    <input type="file" name="filesToUpload[]" id="filesToUpload" multiple>
            <input type="submit" onclick='uploadFile(this)' value="Upload" />
          </div>
        </form>


	<br>	
<%
	BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
%>

	<b>Upload using Blobstore API, for any size: </b>
	<form  id="largeFileForm" action="<%= blobstoreService.createUploadUrl("/upload") %>" method="post" enctype="multipart/form-data">
            <input type="file" name="largeFilesToUpload[]" id="largeFilesToUpload" multiple>
            <input type="submit" value="Upload (using Blobstore API)" onclick='uploadWithBlobstore(this)'>
        </form>

	<br>

	<h3>Write the name of the file to check if it is in the distributed storage system or not:</h3>
	<form name="checkFile">
	   <input type="txt" name="fileToCheck" id="fileToCheckId">
	   <input type="submit" onclick='check(this)' value="Check" />
	</form>
		

<br>------------------------------------------------------------------------------------------------<br>


	<form>

<% 
	// Listing files from Google Cloud Storage
	GcsService gcsService = GcsServiceFactory.createGcsService(new RetryParams.Builder()
	      .initialRetryDelayMillis(10)
	      .retryMaxAttempts(10)
	      .totalRetryPeriodMillis(15000)
	      .build());
	ListResult files = gcsService.list("bgvbucket", null);
	out.println("<br><h3>Files in Google Cloud Storage:</h3>");
	out.println("<p>Click on the file-name to download the file</p><br>");
	if(!files.hasNext())
		out.println("Google Cloud Storage is empty!<br>");
	while(files.hasNext()){		
		ListItem file = files.next();%>
		<a href="/gcs/bgvbucket/<%out.println(file.getName());%>" onclick='return eraserFunction();'><%out.println(file.getName());%></a>
	 	<% out.println("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" + file.getLength() + " Bytes&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");%>
		
		<% // Delete Button %>
		<input type="submit" value="Delete" onclick='deleteFile("<%out.print(file.getName());%>","removesinglefile")' />&nbsp
		
		<%-- //Search for a reular expression in a file key --%>
		<input type="text" id="fExist" name="fExist"/>&nbsp
		<input type="submit" value="FindInFile" onclick='FindInFile("<%out.print(file.getName());%>")' /><br><%
	}

%>


<br>------------------------------------------------------------------------------------------------<br>



	<form>	
<%

	String[] allCachedFiles = null;
	out.println("<br><h3>Files in Cache:</h3>");
	out.println("<p>Click on the file-name to download the file</p><br>");
	// List files from cache
	Cache cache;
	try {
		CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
		cache = cacheFactory.createCache(Collections.emptyMap());
		byte[] value = (byte[]) cache.get("cachedFiles");
		if(value != null){
			String result = new String(value);
			String[] filenames = result.split("//");
			allCachedFiles = filenames;
			int i;
			for(i=0; i<filenames.length; i++){	
				%>
				<a href="/cache?filename=<%out.print(filenames[i]);%>"><%out.print(filenames[i]);%></a>
			 	<%
				//out.println(filenames[i] + "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
				byte[] fileData = (byte[]) cache.get(filenames[i]);
				if(fileData!=null){
					out.println("&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp" + fileData.length + " Bytes&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");%>
					
					<% // Delete Button %>
					<input type="submit" value="Delete" onclick='deleteCachedFile("<%out.print(filenames[i]);%>")' /><br><%
				}
			}
		}
		
	} catch (CacheException e) {
		out.println("Exception in cache :(");
	}
	
%>

	</form>

<br>------------------------------------------------------------------------------------------------<br>

	</form>
	<form action="/index.jsp">
		<h3>Remove all files in Google Cloud Storage: </h3>
		<input type="submit" value="Remove All Files (GCS)" onclick='removeAll()' />&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp

		<h3>Remove all files in Cache: </h3>
		<input type="submit" value="Remove All Files (Cache)" onclick='removeAllCache()' />&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp

		<h3>Find Total Number of Files in Google Cloud Storage: </h3>
		<input type="submit" value="count All Files (GCS)"   onclick='StorageSizeElem()' />&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp

		<h3>Find Total Number of Files in Cache: </h3>
		<input type="submit" value="count All Files (Cache)"   onclick='cacheSizeElem()' />&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp

		<h3>Find total space allocated in Google Cloud Storage: </h3>
		<input type="submit" value="Files Storage Size (GCS)" onclick='StorageSizeMB()' />&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp

		<h3>Find total space allocated in Cache: </h3>
		<input type="submit" value="Files Storage Size (Cache)" onclick='cacheSizeMB()' />&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp


		<h3>Search for regular expression in all files: </h3>
		<input type="text" id="sExist" name="sExist"/>&nbsp
		<input type="submit" value="Search Files" onclick="Listing()" />

	</form>

	<c:set var="uploadTotal" value="${0}" />
	<c:forEach var="fileUploadTime" items="${applicationScope['totalUploadTime']}">
	<c:set var="uploadTotal" value="${uploadTotal + fileUploadTime}" />  
	</c:forEach>

	<%out.println("<br><h3>Total Time Taken:</h3>");%> ${uploadTotal} <% out.println(" ms"); %>

	<c:choose>
	<c:when test="${applicationScope['refreshCount'] == 1}">   
        <% response.setIntHeader("Refresh", 6); %>
		<c:set var="refreshCount" value="${20}"  scope="application" />   
	</c:when>
	<c:otherwise>
		<% response.setIntHeader("Refresh", 26); %>
	</c:otherwise>
	</c:choose>

    </body>


    <script>
	
	function eraserFunction() {
        var request = new XMLHttpRequest();
		request.open("GET", "/delete?eraserTimerList=yes", false); 
		request.send(null);

		var delay=10000;//10 seconds
		setTimeout(function(){  },delay);
		
		return true;
    }
	
	
	// Upload File with Google Cloud API
	function uploadFile() {
		var bucket = "bgvbucket";
		var input = document.getElementById('filesToUpload');
		
		var request = new XMLHttpRequest();
		request.open("GET", "/delete?eraserTimerList=yes", false); 
		request.send(null);

		var delay=30000;//30 seconds
		setTimeout(function(){  },delay);

		for (var x = 0; x < input.files.length; x++) {

			var filename = input.files[x].name;
	
			//Files smaller than and equal to 100KB are cached:
			if(input.files[x].size < 102401){
				var req = new XMLHttpRequest();
				req.open("POST", "/cache?filename=" + filename + "&filesize=" + input.files[x].size , false); //call doPost() from CacheServelet.java, The URL contains filename and filesize as parameters
				req.setRequestHeader("Content-Type", "multipart/form-data");
				req.send(input.files[x]);
			}
			
			//For store to distributed Data set ( Google Cloud Storage )
			var request = new XMLHttpRequest();
			request.open("POST", "/gcs/" + bucket + "/" + filename, false); // URL contains bucketname and filename 
			request.setRequestHeader("Content-Type", "text/plain;charset=UTF-8");
			request.send(input.files[x]);

		}
	}

	//This function is used to upload small files in cache for blobstore api
	function uploadWithBlobstore(){
		var bucket = "bgvbucket";
		var input = document.getElementById('largeFilesToUpload'); //document = index.jsp
		for (var x = 0; x < input.files.length; x++) {

			var filename = input.files[x].name;
	
			//Files smaller than and equal to 100KB are cached:
			if(input.files[x].size < 102401){
				var req = new XMLHttpRequest();
				req.open("POST", "/cache?filename=" + filename + "&filesize=" + input.files[x].size , false);
				req.setRequestHeader("Content-Type", "multipart/form-data");
				req.send(input.files[x]);
			}
		}

	
	}
	//Delete a file in Google Cloud Storage:
	function deleteFile(filename,type){	
		if(type=="removeallfile"){		
			var request = new XMLHttpRequest();
			request.open("GET", "/delete?filename=" + filename, false); // Call doGet() from DeleteServlet.java 
			request.send(null);
		}
		else{		
			var request = new XMLHttpRequest();
			request.open("GET", "/delete?eraserTimerList=yes", false); 
			request.send(null);

			var delay=3000;//30 seconds
			setTimeout(function(){  },delay);
		
			var request = new XMLHttpRequest();
			request.open("GET", "/delete?filename=" + filename, false); // Call doGet() from DeleteServlet.java 
			request.send(null);
		}
	}

	//Delete a file in cache:
	function deleteCachedFile(filename){
		var request = new XMLHttpRequest();
		request.open("GET", "/cachedelete?filename=" + filename, false); // call doGet() from CacheDeleteServelet.java
		request.send(null);
	}

	//Check to see if a file is in the distributed storage system: 
	function check(){
		var filename = document.getElementById("fileToCheckId").value;
		<% 
			//Fetch the names of all files in cache:
			String cacheFilenames = "";
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap()); // Connect to the cache
			byte[] value = (byte[]) cache.get("cachedFiles");
			if(value != null){
				cacheFilenames = new String(value);
			}
		
			//Fetch the names of files in Google Cloud Storage:
			String storageFilenames = "";
			GcsService gcsService2 = GcsServiceFactory.createGcsService(new RetryParams.Builder()
			      .initialRetryDelayMillis(10)
			      .retryMaxAttempts(10)
			      .totalRetryPeriodMillis(15000)
			      .build());
			ListResult files2 = gcsService2.list("bgvbucket", null);
			while(files2.hasNext()){		
				ListItem file = files2.next();
				storageFilenames = file.getName() + "//" + storageFilenames;
			}
			
		%>
		var isInStorage = 0;
		var isInCache = 0;
		
		//Get the results from JSP:
		var cacheFilenames="<%=cacheFilenames%>";
		var storageFilenames="<%=storageFilenames%>";
		
		//Split the results and see if they contain the filename:
		
		//For Google Storage:
		var storageFiles = storageFilenames.split("//");
		for (var x = 0; x < storageFiles.length; x++) {
			if(storageFiles[x].localeCompare(filename) == 0){
				isInStorage = 1;
			} 
		}
		
		//For Cache:
		var cacheFiles = cacheFilenames.split("//");
		for (var x = 0; x < cacheFiles.length; x++) {
			if(cacheFiles[x].localeCompare(filename) == 0){
				isInCache = 1;
			} 
		}
		
		//If filename is empty then the result is false
		if(filename.localeCompare("") == 0){
			isInStorage = 0;
			isInCache = 0;
		}

		// Report the location of file
		if(isInStorage == 1){
			if(isInCache == 1){
				alert("The file is located in both Google Cloud Storage and Cache");
			}
			else{
				alert("The file is located in the Google Cloud Storage only");
			}
		}
		if(isInStorage == 0){
			if(isInCache == 1){
				alert("The file is located in Cache only");
			}
			else{
				alert("The file is not found in the distributed storage system");
			}
		}

	}

	/* extra credits code added here */

	function removeAll(){
	
		var request = new XMLHttpRequest();
		request.open("GET", "/delete?eraserTimerList=yes", false); 
		request.send(null);

		var delay=10000;//10 seconds
		setTimeout(function(){  },delay);
	
		<% ListResult items = gcsService.list("bgvbucket", null);
		while(items.hasNext()){ 
			ListItem item = items.next();%>
			var jar = "<%out.print(item.getName());%>";
			this.deleteFile(jar,"removeallfile");
	    <% } %>
	}

	function removeAllCache(){
		<% if(allCachedFiles != null){
			for(int i=0; i<allCachedFiles.length; i++){
				String filename = allCachedFiles[i];%>
				var filename = "<%out.print(filename);%>";
				this.deleteCachedFile(filename);<%
			}
		} %>
	}

	function cacheSizeElem(){
		<% if(allCachedFiles != null){
			%>
				var jar = "<%out.print(allCachedFiles.length);%>";
				alert("The total number of files in the Cache is " + jar);
			<%
		   }else{
			%>
				alert("There is no file in the cache");
			<%
		} %>
	}

	function cacheSizeMB(){

<%
		
		int totalCacheSize = 0;
		try {
			cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(Collections.emptyMap());
			byte[] values = (byte[]) cache.get("cachedFiles");
			if(value != null){
				String results = new String(values);
				String[] filenamesc = results.split("//");
				int i;
				for(i=0; i<filenamesc.length; i++){	
					byte[] fileContent = (byte[]) cache.get(filenamesc[i]);
					totalCacheSize = totalCacheSize + fileContent.length;
				}
			}
		
		} catch (CacheException e) {
			out.println("Exception in cache :(");
		}
%>
		var jar = "<%out.print(totalCacheSize);%>";
		jar = jar / 1024.0;
		alert("The total number of files in the cache is: " + jar + " KB");

	}
	
	function StorageSizeElem(){
		<% ListResult sitems = gcsService.list("bgvbucket", null);
		int count = 0;
		while(sitems.hasNext()){ 
			ListItem sitem = sitems.next();
			count++;	  
		} %>
		var jar = "<%out.print(count);%>";
		alert("The total number of files in the distributed storage system is: " + jar);
	}

    function StorageSizeMB(){
		<% ListResult titems = gcsService.list("bgvbucket", null);
		double totLength = 0; double totLengthMB;
		while(titems.hasNext()){ 
			ListItem titem = titems.next();
			totLength = totLength + titem.getLength(); %>  
		var jar = "<%out.print(totLength);%>"; 
		<%} totLengthMB = (totLength/(1024*1024)); %>
		var car = <%out.print(totLengthMB);%>;
	    alert("The total space (in MB) allocated to files in the distributed storage system is: " + car + "MB");
    }
	
	function Listing(){
	var word = document.getElementById('sExist').value;
	var array = new Array(411); var x = 0;
	<% ListResult sitems1 = gcsService.list("bgvbucket", null);
	while(sitems1.hasNext()){ %> 
		<% ListItem sitem1 = sitems1.next();
		String str = sitem1.getName(); %>
        var fkey = "<%out.print(str);%>";  
		var res = fkey.match(word);
	    if(res!= null) {  
			array[x] = fkey; 	 
			x++; 							  								 
		 }
		 
	 <% } %>
    if(x > 0) {
		var w = window.open();
		w.document.open();
		w.document.write("List of all file names, whose names match the regular expression string given by user: <br />")
		for(var y = 0; y < x; y++) {
		var jar = array[y];
		w.document.write(jar + '<br />');
		} 
		w.document.close(); 
	}
	else {
			alert("No matching file name found");
		 }
}

	function FindInFile(fkey) {
		var patrn = document.getElementById('fExist').value;
		var mtch = fkey.match(patrn);
		if(mtch!= null) {  
			alert("Match Found"); 							  								 
		}
		else {
			alert("No Match Found");
		}
	}

		
    </script>

</html>



