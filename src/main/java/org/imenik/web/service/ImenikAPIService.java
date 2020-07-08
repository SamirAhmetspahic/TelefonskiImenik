package org.imenik.web.service;

import org.imenik.web.model.Contact;

import java.util.List;

public interface ImenikAPIService {
    Contact addContact(Contact contact);

    Contact getContact(Contact contact);

    List<Contact> getContactList();

    Contact updateContact(Contact contact);

    Contact deleteContact(Contact contact);

}
