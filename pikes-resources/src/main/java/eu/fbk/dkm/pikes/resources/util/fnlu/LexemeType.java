//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.11.12 at 10:43:37 PM CET 
//


package eu.fbk.dkm.pikes.resources.util.fnlu;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for lexemeType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="lexemeType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="POS" use="required" type="{http://framenet.icsi.berkeley.edu}POSType" />
 *       &lt;attribute name="breakBefore" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="headword" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="order" type="{http://framenet.icsi.berkeley.edu}orderType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "lexemeType")
public class LexemeType {

    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "POS", required = true)
    protected POSType pos;
    @XmlAttribute(name = "breakBefore")
    protected Boolean breakBefore;
    @XmlAttribute(name = "headword")
    protected Boolean headword;
    @XmlAttribute(name = "order")
    protected Integer order;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the pos property.
     * 
     * @return
     *     possible object is
     *     {@link POSType }
     *     
     */
    public POSType getPOS() {
        return pos;
    }

    /**
     * Sets the value of the pos property.
     * 
     * @param value
     *     allowed object is
     *     {@link POSType }
     *     
     */
    public void setPOS(POSType value) {
        this.pos = value;
    }

    /**
     * Gets the value of the breakBefore property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isBreakBefore() {
        return breakBefore;
    }

    /**
     * Sets the value of the breakBefore property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setBreakBefore(Boolean value) {
        this.breakBefore = value;
    }

    /**
     * Gets the value of the headword property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isHeadword() {
        return headword;
    }

    /**
     * Sets the value of the headword property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setHeadword(Boolean value) {
        this.headword = value;
    }

    /**
     * Gets the value of the order property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getOrder() {
        return order;
    }

    /**
     * Sets the value of the order property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setOrder(Integer value) {
        this.order = value;
    }

}