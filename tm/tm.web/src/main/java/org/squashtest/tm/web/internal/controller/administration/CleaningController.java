package org.squashtest.tm.web.internal.controller.administration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/administration/cleaning")
public class CleaningController {

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView showCleaningPage() {
		ModelAndView mav = new ModelAndView("page/administration/cleaning");
		return mav;
	}

}
