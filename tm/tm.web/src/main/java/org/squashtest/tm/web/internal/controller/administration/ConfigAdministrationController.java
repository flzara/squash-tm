/**
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
package org.squashtest.tm.web.internal.controller.administration;

import static org.squashtest.tm.web.internal.helper.JEditablePostParams.VALUE;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.squashtest.tm.event.ConfigUpdateEvent;
import org.squashtest.tm.exception.client.ClientNameAlreadyExistsException;
import org.squashtest.tm.service.configuration.ConfigurationService;
import org.squashtest.tm.service.feature.FeatureManager;
import org.squashtest.tm.service.feature.FeatureManager.Feature;
import org.squashtest.tm.service.security.OAuth2ClientService;
import org.squashtest.tm.service.user.UserManagerService;
import org.squashtest.tm.web.internal.i18n.InternationalizationHelper;
import org.squashtest.tm.web.internal.model.datatable.DataTableDrawParameters;
import org.squashtest.tm.web.internal.model.datatable.DataTableModel;

@Controller
@RequestMapping("administration/config")
public class ConfigAdministrationController {

    private static final String WHITE_LIST = "uploadfilter.fileExtensions.whitelist";
    private static final String UPLOAD_SIZE_LIMIT = ConfigurationService.Properties.UPLOAD_SIZE_LIMIT;
    private static final String IMPORT_SIZE_LIMIT = "uploadfilter.upload.import.sizeLimitInBytes";
    @Inject
    private ConfigurationService configService;

    @Inject
    private OAuth2ClientService clientService;

    @Inject
    private InternationalizationHelper messageSource;

    @Inject
    private FeatureManager features;

    @Inject
    private UserManagerService userManager;


	@Inject
	private ApplicationEventPublisher eventPublisher;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public void changeConfig(@RequestParam(WHITE_LIST) String whiteList,
                             @RequestParam(UPLOAD_SIZE_LIMIT) String uploadSizeLimit,
                             @RequestParam(IMPORT_SIZE_LIMIT) String importSizeLimit) {

        configService.updateConfiguration(WHITE_LIST, whiteList);
        configService.updateConfiguration(UPLOAD_SIZE_LIMIT, uploadSizeLimit);
        configService.updateConfiguration(IMPORT_SIZE_LIMIT, importSizeLimit);
        sendUpdateEvent();
    }


    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView administration() {
        ModelAndView mav = new ModelAndView("page/administration/config");

        mav.addObject("whiteList", configService.findConfiguration(WHITE_LIST));
        mav.addObject("uploadSizeLimit", configService.findConfiguration(UPLOAD_SIZE_LIMIT));
        mav.addObject("uploadImportSizeLimit", configService.findConfiguration(IMPORT_SIZE_LIMIT));

        mav.addObject("caseInsensitiveLogin", features.isEnabled(Feature.CASE_INSENSITIVE_LOGIN));
        mav.addObject("duplicateLogins", userManager.findAllDuplicateLogins());

        return mav;
    }

    @RequestMapping(method = RequestMethod.POST, params = {"id=whiteList", VALUE})
    @ResponseBody
    public String changeWhiteList(@RequestParam(VALUE) String newWhiteList) {
        configService.updateConfiguration(WHITE_LIST, newWhiteList);
        sendUpdateEvent();
        return newWhiteList;
    }

    @RequestMapping(method = RequestMethod.POST, params = {"id=uploadSizeLimit", VALUE})
    @ResponseBody
    public String changeUploadSizeLimit(@RequestParam(VALUE) String newUploadSizeLimit) {
        configService.updateConfiguration(UPLOAD_SIZE_LIMIT, newUploadSizeLimit);
        sendUpdateEvent();
        return newUploadSizeLimit;
    }

    @RequestMapping(method = RequestMethod.POST, params = {"id=uploadImportSizeLimit", VALUE})
    @ResponseBody
    public String changeUploadImportSizeLimit(@RequestParam(VALUE) String newUploadImportSizeLimit) {
        configService.updateConfiguration(IMPORT_SIZE_LIMIT, newUploadImportSizeLimit);
        sendUpdateEvent();
        return newUploadImportSizeLimit;
    }


    private void sendUpdateEvent() {
		ConfigUpdateEvent event = new ConfigUpdateEvent("");
		eventPublisher.publishEvent(event);
    }

    @ResponseBody
	@RequestMapping(value = "clients/{idList}", method = RequestMethod.DELETE)
	public
    void removeMilestones(@PathVariable("idList") List<String> idList) {
        clientService.removeClientDetails(idList);
    }

    @ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "clients", method = RequestMethod.POST)
	public
    ClientDetails addClient(@Valid @ModelAttribute("add-client") ClientModel model) {
        BaseClientDetails clientDetails = convertClientModelToBaseClientDetails(model);
        try {
            clientService.addClientDetails(clientDetails);
        } catch (ClientAlreadyExistsException ex) {
            throw new ClientNameAlreadyExistsException(ex);
        }
        return clientDetails;
    }

    private BaseClientDetails convertClientModelToBaseClientDetails(ClientModel model) {
        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setClientId(model.getClientId());
        clientDetails.setClientSecret(model.getClientSecret());
        Set<String> uris = new HashSet<>();
        uris.add(model.getRegisteredRedirectUri());
        clientDetails.setRegisteredRedirectUri(uris);
        return clientDetails;
    }

    @RequestMapping(value = "clients/list")
    @ResponseBody
    public DataTableModel getClientsTableModel(final Locale locale) {

        ClientDataTableModelHelper helper = new ClientDataTableModelHelper(messageSource);
        helper.setLocale(locale);
        Collection<Object> aaData = helper.buildRawModel(clientService.findClientDetailsList());
        DataTableModel model = new DataTableModel("");
        model.setAaData((List<Object>) aaData);
        return model;
    }
}
