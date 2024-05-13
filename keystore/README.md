# Create Release keystore Script
================================
$ keytool -genkey -v -keystore release_key.jks -storepass P9AD74GmVwT68cYApHnJFYTxWsuGtzb7 -alias releasekey -keypass 
 RaUj3WCtBrkcVEcdA4ffSBcxbt7CZ8VA -keyalg RSA -keysize 2048 -validity 1000000

# Get information about Release keystore
========================================
$ keytool -list -v -keystore release_key.jks -storepass P9AD74GmVwT68cYApHnJFYTxWsuGtzb7 -alias releasekey -keypass  RaUj3WCtBrkcVEcdA4ffSBcxbt7CZ8VA

Result:

Alias name: releasekey
Creation date: Aug 15, 2017
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=aleksandr mirko, OU=ltst, O=livetyping, L=omsk, ST=omsk, C=ru
Issuer: CN=aleksandr mirko, OU=ltst, O=livetyping, L=omsk, ST=omsk, C=ru
Serial number: 6a1d82b9
Valid from: Tue Aug 15 11:36:19 OMST 2017 until: Wed Jul 13 11:36:19 OMST 4755
Certificate fingerprints:
	 MD5:  FE:0C:6C:EC:E8:A1:5F:AB:DF:EA:77:51:ED:24:66:2B
	 SHA1: 2A:5B:90:CD:0D:14:E9:03:7D:9E:ED:A9:59:E9:9D:78:CB:6D:2A:93
	 SHA256: 0B:BF:2E:38:C7:05:F8:2E:1E:39:05:0E:73:23:18:61:48:AD:C8:34:E2:8D:E3:1F:A0:0F:99:E6:11:F5:46:30
	 Signature algorithm name: SHA256withRSA
	 Version: 3
	 
# Get information about Debug keystore
========================================
$ keytool -list -v -keystore debug_key.jks -storepass u2aJKSpAZNteY34MzqdrDP4wrgz8QaeR -alias debugkey -keypass  
KbmnesFGdn6bBgvqJ9pwBrbZ5dRmpmu9

Result:

Alias name: debugkey
Creation date: May 11, 2017
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: O=LiveTyping
Issuer: O=LiveTyping
Serial number: 4de8fc6c
Valid from: Thu May 11 19:00:35 OMST 2017 until: Mon May 05 19:00:35 OMST 2042
Certificate fingerprints:
	 MD5:  BF:B9:F3:4A:EF:C7:58:47:08:6B:D2:7D:A6:56:3D:DB
	 SHA1: 7A:C4:AE:93:65:06:FA:D3:27:9F:EC:E7:10:8F:36:12:A6:55:71:52
	 SHA256: 88:19:44:B9:0C:4A:8D:8E:87:2D:77:EB:6E:95:2C:AE:0B:67:34:C7:33:F9:4D:08:7A:07:51:15:16:0B:2E:0A
	 Signature algorithm name: SHA256withRSA
	 Version: 3