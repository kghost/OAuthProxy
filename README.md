# OAuthProxy Manual #

## 1. How to Use an Existing OAuthProxy ##

### 1.1. Requirement ###

1. A client support OAuth and can change api url. (chromed_bird, also...)
1. A OAuthProxy server support the client you use. (you can setup your own on GAE, see next section)

### 1.2. Setup Client ###

Assume you will use OAuthProxy at https://your-proxy.example.com/

First change client api url to https://your-proxy.example.com/1/, and oauth url to https://your-proxy.example.com/oauth/. Then open your client, usually it will take three steps to complete oauth progress.


Step.1 Request OAuth Token

This step will be done automatically once start login progress, and won't promote anything. At end of this step, the client should provide a authorize url or open a browser page like https://your-proxy.example.com/oauth/authentication?token=xxxxxxxxxxxxxxxxxxxxx, it is used by 2nd step.

Step.2 Authorize the Server

**NOTE: This is the only step need direct access to the actual host of the proxy, and secure connection(https) is strongly recommended.**

Now open the url taken from 1st step, it will redirect to actual host of the proxy, login your account and grant the access. The site should return a pin code used by last step.

Step.3 Get Access Token

Enter the pin code taken from previous step in client, enjoy yourself if nothing wrong happened. **If something wrong happened after entered the pin code, please restart from 1st step, the pin code can't be used once more**

## 2. How to Setup OAuthProxy on GAE Server ##

### 2.1. Requirement ###

1. The consumer secret of clients the proxy support. If the secret is unknown, the proxy **CAN'T** proxy requests from this kind of client. You can find it easily if it is open source client, or do reverse engineering.
2. An Google App Engine account.
3. JDK 1.5+. Please download form java.sun.com. (I'm using openjdk 1.6 and don't known if 1.5 works, but it should work)
4. maven2 (recommend), ant or eclipse.

### 2.2. Preparation ###

##### First download code from github. #####

    git clone git://github.com/kghost/OAuthProxy.git

##### Then request an consumer token/secret. (optional, but strongly recommended, the hard coded token is public known, it is not secure) #####
([here for twitter](https://twitter.com/apps). For "Application Type" choose client not browser, leave callback url empty)  
Open OAuthProxy/war/WEB-INF/web.xml, find this block

     <filter>
       <filter-name>OAuthSignFilter</filter-name>
       <filter-class>name.kghost.oauth.filter.OAuthSignFilter</filter-class>
       <init-param>
         <param-name>token</param-name>
         <param-value>i1yxg2pFEnJSJ7cihtLUzQ</param-value>
       </init-param>
       <init-param>
         <param-name>method</param-name>
         <param-value>HMAC-SHA1</param-value>
       </init-param>
       <init-param>
         <param-name>secret</param-name>
         <param-value>KHA4tKlHYtuO5j02k0HV4AceObdTiVitH9UMuqkuF24</param-value>
       </init-param>
     </filter>

change the token/secret to what you got yet, leave method away.


##### Change application id #####
Open war/WEB-INF/appengine-web.xml, find  

    <application>zealot-tw</application>

change to your application id, *it is application id, not google account name.*

### 2.3. Compile and Deploy to GAE Server ###

You have 3 choices, use Maven, Ant, or Eclipse.

#### 2.3.1. Setup a GAE Server using Maven ####

Enter OAuthProxy directory, run

    mvn gae:deploy

then maven will do the whole work.

If it is the first time use maven gae, you should run

    mvn gae:unpack

to downlaod google appengine sdk to maven repository.

#### 2.3.2. Setup a GAE Server using Ant ####

Download Google App Engine SDK for Java from [here](http://code.google.com/appengine/downloads.html), unpack to somewhere.

Edit build.xml, change the location of sdk.dir to where you unpack the sdk:

    <property name="sdk.dir" location="../appengine-java-sdk" />

Enter OAuthProxy directory, run

    ant compile
    ant update_indexes
    ant update

then Ant will compile the source code and deploy OAuthProxy to GAE.

**It is not done, you must configure it before use, see next**

#### 2.3.3. Setup a GAE Server using Eclipse ####

Please follow google official instruction.

**It is not done, you must configure it before use, see next**

### 2.4. Configure OAuthProxy ###

Open https://your-application-id.appspot.com/config, add client consumer token/secret here, you can add multiple clients.
(past consumer token/secret, click "+" to the left)

**Contact me on twitter if have questions @zealot0630**

