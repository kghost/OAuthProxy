//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.2-147 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.04.15 at 11:46:07 午後 CST 
//


package name.kghost.oauth.config;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for OAuthUser complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="OAuthUser">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Token" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="Secret" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OAuthUser", propOrder = {
    "token",
    "secret"
})
@PersistenceCapable
public class OAuthUser {

    @XmlElement(name = "Token", required = true)
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    protected String token;
    @XmlElement(name = "Secret", required = true)
    @Persistent
    protected String secret;

    public OAuthUser(String token, String secret) {
    	this.token = token;
    	this.secret = secret;
    }

	/**
     * Gets the value of the token property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets the value of the token property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setToken(String value) {
        this.token = value;
    }

    /**
     * Gets the value of the secret property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Sets the value of the secret property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSecret(String value) {
        this.secret = value;
    }

}