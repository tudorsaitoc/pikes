//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.12 at 03:35:52 PM CET 
//


package eu.fbk.dkm.pikes.resources.util.onsenses;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "grSense",
    "wn",
    "omega",
    "pb",
    "vn",
    "fn"
})
@XmlRootElement(name = "mappings")
public class Mappings {

    @XmlElement(name = "gr_sense")
    protected String grSense;
    @XmlElement(required = true)
    protected List<Wn> wn;
    @XmlElement(required = true)
    protected String omega;
    @XmlElement(required = true)
    protected String pb;
    protected String vn;
    protected String fn;

    /**
     * Gets the value of the grSense property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGrSense() {
        return grSense;
    }

    /**
     * Sets the value of the grSense property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGrSense(String value) {
        this.grSense = value;
    }

    /**
     * Gets the value of the wn property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the wn property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getWn().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Wn }
     * 
     * 
     */
    public List<Wn> getWn() {
        if (wn == null) {
            wn = new ArrayList<Wn>();
        }
        return this.wn;
    }

    /**
     * Gets the value of the omega property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOmega() {
        return omega;
    }

    /**
     * Sets the value of the omega property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOmega(String value) {
        this.omega = value;
    }

    /**
     * Gets the value of the pb property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPb() {
        return pb;
    }

    /**
     * Sets the value of the pb property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPb(String value) {
        this.pb = value;
    }

    /**
     * Gets the value of the vn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVn() {
        return vn;
    }

    /**
     * Sets the value of the vn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVn(String value) {
        this.vn = value;
    }

    /**
     * Gets the value of the fn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFn() {
        return fn;
    }

    /**
     * Sets the value of the fn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFn(String value) {
        this.fn = value;
    }

}