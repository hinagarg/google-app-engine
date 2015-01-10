1. Source Code in Data Set Generator folder is used to generate the large set of random files of different sizes once the code is executed.

2. Source Code inside Project Source Code folder creates a web interface which uses the random set of files generated in first step to perform various operations of uploading/ downloading, searching, listing and removing files by using Google App Engine once deployed.

3. Website is currently hosted at: http://3.swift-casing-752.appspot.com/

4. Large files are placed in Google Cloud Storage while smaller files(less than 100KB)are placed in Cache.