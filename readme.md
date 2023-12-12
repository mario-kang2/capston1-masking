Masqurade
=========
Privacy Masking Solution
## How to Install
### Spring
* Requirements
  - Java 21 or higher
  - Apache Tomcat 11 or higher
  - Visual Studio Code is recommended
* Deploy WAR files
* Make these directories:
  - <pre><code>/home/tomcat/upload-dir</code></pre>
  - <pre><code>/home/tomcat/masked-dir</code></pre>
  - Permission of these directories MUST be <pre><code>666</code></pre>
### Flask Server
* Requirements
  - PyTorch
  - OpenCV-Python
  - requests
  - Flask
  - uWSGI
  - <pre><code>pip3 install -r requirements.txt</code></pre> is recommended
* Open the server to <pre><code>nohup uwsgi --ini uwsgi.ini --master > /dev/null 2>&1 &</code></pre>
* Port number of Flask server MUST be <pre><code>48080</code></pre>
