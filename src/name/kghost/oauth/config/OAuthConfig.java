//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.04.15 at 11:46:07 午後 CST 
//


package name.kghost.oauth.config;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OAuthConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OAuthConfig">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="consumers" type="{}OAuthConsumer" maxOccurs="unbounded"/>
 *         &lt;element name="users" type="{}OAuthUser" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OAuthConfig", propOrder = {
    "consumers",
    "users"
})
public class OAuthConfig {

    @XmlElement(required = true)
    protected List<OAuthConsumer> consumers;
    @XmlElement(required = true)
    protected List<OAuthUser> users;

    /**
     * Gets the value of the consumers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the consumers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getConsumers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OAuthConsumer }
     * 
     * 
     */
    public List<OAuthConsumer> getConsumers() {
        if (consumers == null) {
            consumers = new ArrayList<OAuthConsumer>();
        }
        return this.consumers;
    }

    /**
     * Gets the value of the users property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the users property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getUsers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OAuthUser }
     * 
     * 
     */
    public List<OAuthUser> getUsers() {
        if (users == null) {
            users = new ArrayList<OAuthUser>();
        }
        return this.users;
    }

}
