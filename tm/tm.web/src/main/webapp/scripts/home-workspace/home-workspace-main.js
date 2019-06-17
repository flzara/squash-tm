/*
 *     This file is part of the Squashtest platform.
 *     Copyright (C) Henix, henix.fr
 *
 *     See the NOTICE file distributed with this work for additional
 *     information regarding copyright ownership.
 *
 *     This is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     this software is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
define(['./home-main-view', 'jquery', 'squash.translator'],
    function (MainView, $, translator) {
        "use strict";

         function init() {
					  initInformationBlock();
         		new MainView();
         }

         function initInformationBlock(){
         	 var informationBlock = $("#information-block");
         	 if (informationBlock != null){
				var dateMessage = initDateMessage();
				var userMessage = initUserMessage();
				if(dateMessage != null){
					appendMessage(dateMessage);
				}
				if(userMessage != null){
					appendMessage(userMessage);
				}
				informationBlock.show();
			}
		 }

		 function initDateMessage() {
         	var dateMessage;

			var dateLicenseInformation = squashtm.app.dateLicenseInformation;

			if(dateLicenseInformation !== null && dateLicenseInformation !== ''){
				var daysRemaining = parseInt(dateLicenseInformation);
				if(daysRemaining < 0) {
					dateMessage = translator.get("information.expirationDate.warning3", getExpirationDate(daysRemaining), getExpirationDatePlus2Months(daysRemaining));
				} else if (daysRemaining < 30){
					dateMessage = translator.get("information.expirationDate.warning2", getExpirationDate(daysRemaining), getExpirationDatePlus2Months(daysRemaining));
				} else if (daysRemaining < 61){
					dateMessage = translator.get("information.expirationDate.warning1", getExpirationDate(daysRemaining));
				}
			}
			return dateMessage;
		 }

		 function getExpirationDate(daysRemaining){
			 var currentDate = new Date();
			 var expirationDate = new Date();
			 expirationDate.setDate(currentDate.getDate() + parseInt(daysRemaining));
			 return expirationDate.toLocaleDateString(undefined, {
				 day: '2-digit',
				 month: '2-digit',
				 year: 'numeric'
			 });
		 }

			function getExpirationDatePlus2Months(daysRemaining){
				var currentDate = new Date();
				var expirationDate = new Date();
				expirationDate.setDate(currentDate.getDate() + parseInt(daysRemaining) + 61);
				return expirationDate.toLocaleDateString(undefined, {
					day: '2-digit',
					month: '2-digit',
					year: 'numeric'
				});
			}

		 function initUserMessage() {
			var userLicenseInformation = squashtm.app.userLicenseInformation;
			 var userMessage;
			if(userLicenseInformation !== null && userLicenseInformation !== ''){
				var userLicenseInformationArray = userLicenseInformation.split("-");
				var activeUsersCount = userLicenseInformationArray[0];
				var maxUsersAllowed = userLicenseInformationArray[1];
				var allowCreateUsers = JSON.parse(userLicenseInformationArray[2]);
				if (!allowCreateUsers){
					userMessage = translator.get("information.userExcess.warning2", maxUsersAllowed, activeUsersCount);
				} else {
					userMessage = translator.get("information.userExcess.warning1", maxUsersAllowed, activeUsersCount);
				}
			}

			return userMessage;
		 }

		 function appendMessage(message){
         	var informationBlockContent = $("#information-block-content");
         	var element = document.createElement("p");
         	element.innerHTML = message;
         	informationBlockContent.append(element);
		 }

         return {
             init : init
         };
    });
