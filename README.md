# 4sharedAndroid

An Android SDK that consumes 4shared file storage service.

![Demo Screenshot 1](https://github.com/jayxue/4sharedAndroid/blob/master/4sharedAndroidSDK/src/main/res/raw/screenshot_1.png)

Details
-------
4shared (www.4shared.com) is one of the highest-quality, free online hosting and sharing services. This library helps developers easily create Android applications that consume 4shared service for browsing, creating, updating and deleting files.

A sample application is provided to show:
* Taking or picking up and previewing a video on a device.
* Uploading the video to 4shared and getting direct download link of the uploaded file. Upload progress is displayed on UI.
* Updating the same file with a new video.
* Deleting the file.

Special Note
------------
The library worked well with 4shared's SOAP service. Unfortunately, 4shared just retired the SOAP service and released their first REST service recently. Therefore, the library is not able to access 4shared service anymore.

An updated library that consumes 4shared's REST service will be provided soon.

Developer
---------
* Jay Xue <yxue24@gmail.com>, Waterloo Mobile Studio

License
-------

    Copyright 2015 Waterloo Mobile Studio

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
