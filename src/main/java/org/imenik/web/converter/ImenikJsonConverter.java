package org.imenik.web.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.imenik.web.model.Contact;

import javax.annotation.ManagedBean;
import java.util.List;
import org.apache.log4j.Logger;

@ManagedBean
public class ImenikJsonConverter {

    static Logger LOG = Logger.getLogger(ImenikJsonConverter.class.getName());

    public Contact convertToContact(String json) {
        Contact contact = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            contact = mapper.readValue(json, Contact.class);
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }
        return contact;
    }

    public List<Contact> convertToContactList(String json) {
        List<Contact> contact = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            contact = mapper.readValue(json, new TypeReference<List<Contact>>() {
            });
        } catch (JsonProcessingException e) {
            LOG.error(e.getMessage());
        }
        return contact;
    }
}
