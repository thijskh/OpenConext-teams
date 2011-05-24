/*
 * Copyright 2011 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

COIN.MODULES.Addteam = function(sandbox) {
	// Public interface
	var module = {
		init: function() {
			// Clicked [ Cancel ]
			$('input[name=cancelCreateTeam]').live('click', function(e) {
				e.preventDefault();
        var view = $('input[name=view]').val();
				sandbox.redirectBrowserTo('home.shtml?teams=my&view=' + view);
			});
			
			// Clicked [ Consent ]
			$('input[name=consent]').live('click', function(e) {
			  library.toggleDisable($('input[name=createTeam]'));
			});
			
			$('#TeamName').focus();
		},
		
		destroy: function() {
			
		}
	};
	
	// Private library (through closure)
	var library = {
			toggleDisable : function(el) {
			  if (el instanceof jQuery) {
			    if (el.attr('disabled') == false) {
			      el.removeClass('button-primary').addClass('button-disabled');
			      el.attr('disabled', true);
			    } else {
			      el.removeAttr('disabled');
			      el.removeClass('button-disabled').addClass('button-primary');
			    }
			  }
			}
	};

	// Return the public interface
	return module;
};