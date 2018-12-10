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
 *  Smart Nightlight
 *
 *  Author: SmartThings and slightly modified to remove the light sensor.
 *
 */

definition(
    name: "Smart Nightlight Modified Virtual",
    namespace: "smartthings",
    author: "SmartThings",
    description: "Turns on lights when between sunset and sunrise and motion is detected. Turns lights off some time after motion ceases. As long as virtual switch is on",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/light_motion-outlet-luminance@2x.png"
)

preferences {
	section("Control these lights..."){
		input "lights", "capability.switch", multiple: true
	}
	section("Turning on when between sunset and sunrise and there's movement..."){
		input "motionSensor", "capability.motionSensor", title: "Where?"
	}
	section("And then off there's been no movement for..."){
		input "delayMinutes", "number", title: "Minutes?"
	}
    section("Only if the virtual switch is on..."){
    	input "vswitch", "capability.switch"
    }
	section ("Sunrise offset (optional)...") {
		input "sunriseOffsetValue", "text", title: "HH:MM", required: false
		input "sunriseOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Sunset offset (optional)...") {
		input "sunsetOffsetValue", "text", title: "HH:MM", required: false
		input "sunsetOffsetDir", "enum", title: "Before or After", required: false, options: ["Before","After"]
	}
	section ("Zip code (optional, defaults to location coordinates when location services are enabled)...") {
		input "zipCode", "text", title: "Zip code", required: false
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	subscribe(motionSensor, "motion", motionHandler)
	subscribe(location, "position", locationPositionChange)
    subscribe(location, "sunriseTime", sunriseSunsetTimeHandler)
	subscribe(location, "sunsetTime", sunriseSunsetTimeHandler)
    astroCheck()
}

def locationPositionChange(evt) {
	log.trace "locationChange()"
	astroCheck()
}

def sunriseSunsetTimeHandler(evt) {
	state.lastSunriseSunsetEvent = now()
	log.debug "SmartNightlight.sunriseSunsetTimeHandler($app.id)"
	astroCheck()
}

def motionHandler(evt) {
	log.debug "$evt.name: $evt.value"
	if (evt.value == "active") {
		if (enabled()) {
			log.debug "turning on lights due to motion"
			lights.on()
			state.lastStatus = "on"
		}
		state.motionStopTime = null
	}
	else {
		state.motionStopTime = now()
		if(delayMinutes) {
			runIn(delayMinutes*60, turnOffMotionAfterDelay, [overwrite: true])
		} else {
			turnOffMotionAfterDelay()
		}
	}
}
				
def turnOffMotionAfterDelay() {
	log.trace "In turnOffMotionAfterDelay, state.motionStopTime = $state.motionStopTime, state.lastStatus = $state.lastStatus"
	if (state.motionStopTime && state.lastStatus != "off") {
		def elapsed = now() - state.motionStopTime
        log.trace "elapsed = $elapsed"
		if (elapsed >= ((delayMinutes ?: 0) * 60000L) - 2000) {
        	log.debug "Turning off lights"
			lights.off()
			state.lastStatus = "off"
		}
	}
}

def astroCheck() {
	def s = getSunriseAndSunset(zipCode: zipCode, sunriseOffset: sunriseOffset, sunsetOffset: sunsetOffset)
	state.riseTime = s.sunrise.time
	state.setTime = s.sunset.time
	log.debug "rise: ${new Date(state.riseTime)}($state.riseTime), set: ${new Date(state.setTime)}($state.setTime)"
}


private enabled() {
	def result
    def fakeswitch = vswitch.currentValue("switch")
    log.debug "$app.name: $vswitch.label: $fakeswitch"
    def fakevalue = false
    if (fakeswitch == "on"){
       fakevalue = true
    } else {
       fakevalue = false
    }
    
	def t = now()
	result = t < state.riseTime || t > state.setTime
	log.debug "now: ${new Date(t)} ($t) and result: $result"
    sendNotificationEvent("$app.name: before $result")
    
    result = result && fakevalue
    sendNotificationEvent("$app.name: $vswitch.label: $fakeswitch: $fakevalue with result: $result")
    
	result
}

private getSunriseOffset() {
	sunriseOffsetValue ? (sunriseOffsetDir == "Before" ? "-$sunriseOffsetValue" : sunriseOffsetValue) : null
}

private getSunsetOffset() {
	sunsetOffsetValue ? (sunsetOffsetDir == "Before" ? "-$sunsetOffsetValue" : sunsetOffsetValue) : null
}