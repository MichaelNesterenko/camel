/* Generated by camel build tools - do NOT edit this file! */
package org.apache.camel.main;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.ExtendedPropertyConfigurerGetter;
import org.apache.camel.spi.PropertyConfigurerGetter;
import org.apache.camel.spi.ConfigurerStrategy;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.util.CaseInsensitiveMap;
import org.apache.camel.main.AwsVaultConfigurationProperties;

/**
 * Generated by camel build tools - do NOT edit this file!
 */
@SuppressWarnings("unchecked")
public class AwsVaultConfigurationPropertiesConfigurer extends org.apache.camel.support.component.PropertyConfigurerSupport implements GeneratedPropertyConfigurer, PropertyConfigurerGetter {

    @Override
    public boolean configure(CamelContext camelContext, Object obj, String name, Object value, boolean ignoreCase) {
        org.apache.camel.main.AwsVaultConfigurationProperties target = (org.apache.camel.main.AwsVaultConfigurationProperties) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "accesskey":
        case "AccessKey": target.setAccessKey(property(camelContext, java.lang.String.class, value)); return true;
        case "awsvaultconfiguration":
        case "AwsVaultConfiguration": target.setAwsVaultConfiguration(property(camelContext, org.apache.camel.vault.AwsVaultConfiguration.class, value)); return true;
        case "defaultcredentialsprovider":
        case "DefaultCredentialsProvider": target.setDefaultCredentialsProvider(property(camelContext, boolean.class, value)); return true;
        case "region":
        case "Region": target.setRegion(property(camelContext, java.lang.String.class, value)); return true;
        case "secretkey":
        case "SecretKey": target.setSecretKey(property(camelContext, java.lang.String.class, value)); return true;
        default: return false;
        }
    }

    @Override
    public Class<?> getOptionType(String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "accesskey":
        case "AccessKey": return java.lang.String.class;
        case "awsvaultconfiguration":
        case "AwsVaultConfiguration": return org.apache.camel.vault.AwsVaultConfiguration.class;
        case "defaultcredentialsprovider":
        case "DefaultCredentialsProvider": return boolean.class;
        case "region":
        case "Region": return java.lang.String.class;
        case "secretkey":
        case "SecretKey": return java.lang.String.class;
        default: return null;
        }
    }

    @Override
    public Object getOptionValue(Object obj, String name, boolean ignoreCase) {
        org.apache.camel.main.AwsVaultConfigurationProperties target = (org.apache.camel.main.AwsVaultConfigurationProperties) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
        case "accesskey":
        case "AccessKey": return target.getAccessKey();
        case "awsvaultconfiguration":
        case "AwsVaultConfiguration": return target.getAwsVaultConfiguration();
        case "defaultcredentialsprovider":
        case "DefaultCredentialsProvider": return target.isDefaultCredentialsProvider();
        case "region":
        case "Region": return target.getRegion();
        case "secretkey":
        case "SecretKey": return target.getSecretKey();
        default: return null;
        }
    }
}

