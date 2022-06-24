package com.arktech.reddit.service;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import lombok.AllArgsConstructor;

/**
 * 
 * @author alimi
 * This class contains the method build() which takes our email message as input and 
 * it uses the Thymeleaf‘s TemplateEngine to generate the email message.
 *
 * mailTemplate as an argument to the method call templateEngine.process(“mailTemplate”, context); 
 * That would be the name of the HTML template which looks like below:
 */

@Service
@AllArgsConstructor
public class MailContentBuilder {

	private TemplateEngine templateEngine;
	
	String build(String message) {
		Context context = new Context();
		context.setVariable("message", message);
		
		return templateEngine.process("mailTemplate", context);
	}
}
