package org.imenik.web.service.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.imenik.web.converter.ImenikJsonConverter;
import org.imenik.web.exception.NullContactException;
import org.imenik.web.model.Contact;
import org.imenik.web.service.ImenikAPIService;
import org.primefaces.context.RequestContext;
import org.primefaces.json.JSONObject;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.net.URI;
import java.util.List;

@Named
@ManagedBean(name = "apiService", eager = true)
@SessionScoped
public class DefaultImenikAPIService implements ImenikAPIService {

    private static final String DEFAULT_USERNAME = "srcuser";
    private static final String DEFAULT_PASSWORD = "srcpwd";

    private static final String CONTENT_TYPE = "application/json";

    private static final String BASE_API_URL = "http://localhost:8080/ImenikAPI/api/v1/resources/";
    private static final String GET_CONTACT_URL = BASE_API_URL + "contact";
    private static final String GET_CONTACTS_URL = BASE_API_URL + "contacts";
    private static final String ADD_CONTACT_URL = BASE_API_URL + "add-contact";
    private static final String UPDATE_CONTACT_URL = BASE_API_URL + "update-contact";
    private static final String DELETE_CONTACT_URL = BASE_API_URL + "delete-contact";

    static Logger LOG = Logger.getLogger(DefaultImenikAPIService.class.getName());

    private Contact contact;
    private List<Contact> contactList;

    @Inject
    private ImenikJsonConverter imenikJsonConverter;

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    private CloseableHttpClient getCloseableHttpClient() {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        return httpclient;
    }

    private CredentialsProvider getCredentialsProvider(String username, String password) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(DEFAULT_USERNAME, DEFAULT_PASSWORD));
        return credentialsProvider;
    }

    private HttpClientContext getClientContext(String username, String password) {
        HttpClientContext localContext = HttpClientContext.create();
        localContext.setCredentialsProvider(getCredentialsProvider(username, password));
        return localContext;
    }

    @Override
    public Contact addContact(Contact contact) {
        System.out.println("####################### Contact is " + contact.getAddress() + " | " + contact.getDescription());
        HttpPost httpPost = new HttpPost();
        return persistUpdateHelper(contact, ADD_CONTACT_URL, httpPost);
    }

    /**
     * Get single contact
     *
     * @param contact
     * @return {@link Contact}
     */
    @Override
    public Contact getContact(Contact contact) {
        LOG.debug("Fetch single contact started.");
        CloseableHttpResponse response = null;
        String responseString = null;
        HttpGet httpget = new HttpGet(GET_CONTACT_URL + "/" + contact.getId());
        try {
            response = getCloseableHttpClient().execute(httpget, getClientContext(DEFAULT_USERNAME, DEFAULT_PASSWORD));
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new NullContactException(String.format("Contact is not present for specified id:%d", contact.getId()));
            }
            responseString = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
        Contact responseContact = imenikJsonConverter.convertToContact(responseString);
        String stringMessage = messageBuiler(responseContact);
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, stringMessage, "Kontakt");
        FacesContext.getCurrentInstance().addMessage(null, message);

        return responseContact;
    }

    /**
     * Build contact message in case: get single contact by id
     *
     * @param contact
     * @return {@link String}
     */
    private String messageBuiler(Contact contact) {
        StringBuilder builder = new StringBuilder("Contact id: " + contact.getId() + "\n");
        builder.append("\n Ime:            " + contact.getName());
        builder.append("\n Priimek:        " + contact.getLastName());
        builder.append("\n Naslov:         " + contact.getAddress());
        builder.append("\n Email:          " + contact.getEmail());
        builder.append("\n Telefon:        " + contact.getPhone());
        builder.append("\n Mobilni:        " + contact.getMobile());
        builder.append("\n Podrobnosti:    " + contact.getDescription());
        builder.append("\n Kreirano:       " + contact.getCreatedAt());
        builder.append("\n Posodobljeno:   " + contact.getUpdatedAt());
        return builder.toString();
    }

    /**
     * Retrieve all contacts
     *
     * @return {@link List<Contact>}
     */
    @Override
    public List<Contact> getContactList() {
        LOG.debug("Fetch contact list started.");
        CloseableHttpResponse response = null;
        String responseString = null;
        HttpGet httpget = new HttpGet(GET_CONTACTS_URL);
        try {
            response = getCloseableHttpClient().execute(httpget, getClientContext(DEFAULT_USERNAME, DEFAULT_PASSWORD));
            HttpEntity entity = response.getEntity();
            responseString = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
        this.contactList = imenikJsonConverter.convertToContactList(responseString);
        return contactList;
    }

    /**
     * Update contact
     *
     * @param contact
     * @return {@link Contact}
     */
    @Override
    public Contact updateContact(Contact contact) {
        LOG.debug("Contact update started.");
        HttpPut httpPut = new HttpPut();
        return persistUpdateHelper(contact, UPDATE_CONTACT_URL, httpPut);
    }

    /**
     * Delete contact
     *
     * @param contact
     * @return {@link Contact}
     */
    @Override
    public Contact deleteContact(Contact contact) {
        LOG.debug("Delete contact started.");
        CloseableHttpResponse response = null;
        String responseString = null;

        HttpDelete httpDelete = new HttpDelete(DELETE_CONTACT_URL + "/" + contact.getId());
        String encoding = org.apache.commons.codec.binary.Base64.encodeBase64String((DEFAULT_USERNAME + ":" + DEFAULT_PASSWORD).getBytes());
        httpDelete.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);
        httpDelete.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
        try {
            response = getCloseableHttpClient().execute(httpDelete, getClientContext(DEFAULT_USERNAME, DEFAULT_PASSWORD));
            if (response == null) {
                throw new NullContactException("Specified contact does not exist.");
            }
            HttpEntity entity = response.getEntity();
            responseString = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
        return imenikJsonConverter.convertToContact(responseString);
    }

    /*
     * Helper method for persist and update
     *
     * @param contact
     * @param URL
     * @param httpMethod
     *
     * @return {@link Contact}
     *
     */
    public Contact persistUpdateHelper(Contact contact, String URL, HttpEntityEnclosingRequestBase httpMethod) {
        CloseableHttpResponse response = null;
        String responseString = null;

        JSONObject json = new JSONObject();
        if (httpMethod instanceof HttpPut) {
            json.put("id", contact.getId());
        }
        json.put("lastName", contact.getLastName());
        json.put("name", contact.getName());
        json.put("email", contact.getEmail());
        json.put("description", contact.getDescription());
        json.put("address", contact.getAddress());
        json.put("phone", contact.getPhone());
        json.put("mobile", contact.getMobile());

        String encoding = org.apache.commons.codec.binary.Base64.encodeBase64String((DEFAULT_USERNAME + ":" + DEFAULT_PASSWORD).getBytes());
        httpMethod.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);

        httpMethod.setHeader(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
        StringEntity contactEntityString = new StringEntity(json.toString(), "UTF-8");
        httpMethod.setEntity(contactEntityString);
        httpMethod.setURI(URI.create(URL));
        try {
            response = getCloseableHttpClient().execute(httpMethod, getClientContext(DEFAULT_USERNAME, DEFAULT_PASSWORD));
            HttpEntity entity = response.getEntity();
            responseString = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            LOG.error(e.getMessage());
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
        return imenikJsonConverter.convertToContact(responseString);
    }

    public void setSelected(Contact c) {
        contact = getContact(c);
        RequestContext.getCurrentInstance().update("dialog:dialog-form");
    }
}
