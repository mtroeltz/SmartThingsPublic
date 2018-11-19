/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Notify Me When It Opens
 *
 *  Author: SmartThings
 */
definition(
    name: "Notify Me When It Opens",
    namespace: "mtroeltz",
    author: "modified from SmartThings",
    description: "Get a text message sent to your phone when an open/close sensor is opened.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/window_contact@2x.png"
)

preferences {
	section("When the door opens..."){
		input "door", "capability.contactSensor", title: "Where?", multiple:true
	}
    section("Send Notifications?") {
        input("recipients", "contact", title: "Send notifications to") {
            input "phone1", "phone", title: "Notify via SMS",
                description: "Phone Number", required: true
        }
    }
	section("Optional Receipient 2") {
       	input "phone2", "phone", title: " (optional)",
         	description: "Phone Number", required: false
    }
    
	section("Optional Receipient 3") {
       	input "phone3", "phone", title: " (optional)",
         	description: "Phone Number", required: false
    }
    
	section("Optional Receipient 4") {
       	input "phone4", "phone", title: " (optional)",
         	description: "Phone Number", required: false
    }
    
    }

def installed()
{
	subscribe(door, "contact.open", contactOpenHandler)
    //log.debug "installed: $phone1 and $phone2"
}

def updated()
{
	unsubscribe()
	subscribe(door, "contact.open", contactOpenHandler)
    //log.debug "updated: $phone1 and $phone2"
}

def contactOpenHandler(evt) {
	log.trace "$evt.value: $evt, $settings"
	//log.debug "contactOpenHandler: p1: $phone1 p2:$phone2 p3:$phone3 p4:$phone4"
    //log.debug "door size: ${door.size()}"
  	//log.debug "door: $door.currentContact"

    def message = ""

	// iterate through each door to find open doors
	door.each { mydoor ->
   		// log.debug "$mydoor: $mydoor.currentContact"  
    	if (mydoor.currentContact == "open") {
   		  	message = message + "$mydoor was opened\n"
			log.debug "$mydoor was opened"
   	 }
   	}
    
    def allPhones = ["$phone1","$phone2","$phone3","$phone4"]
    //log.debug "allPhones: $allPhones"
    allPhones.each { myphone ->
        //log.debug "in allPhones each: $myphone"
        if (myphone.length() > 9){
   			sendSms(myphone, message)
            log.debug "sent message to $myphone"
        }
   }
}