/*
 * Copyright (c) 2016 The Hyve B.V.
 * This code is licensed under the GNU Affero General Public License (AGPL),
 * version 3, or (at your option) any later version.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.authentication.saml;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.authentication.PortalUserDetails;
import org.mskcc.cbio.portal.dao.PortalUserDAO;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.opensaml.saml2.core.Attribute;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;

/**
 * Custom UserDetailsService which parses SAML messages and checks authorization of
 * user against cbioportal's `authorities` configuration. Authentication is done by the SAML IDP.
 *
 * @author Pieter Lukasse
 */
public class SAMLUserDetailsServiceImpl implements SAMLUserDetailsService
{
    private static final Log log = LogFactory.getLog(SAMLUserDetailsServiceImpl.class);
    private final PortalUserDAO portalUserDAO;

    /**
     * Constructor.
     *
     * Takes a ref to PortalUserDAO used to check authorization of registered
     * users in the database.
     *
     * @param portalUserDAO PortalUserDAO
     */
    public SAMLUserDetailsServiceImpl(PortalUserDAO portalUserDAO) {
        this.portalUserDAO = portalUserDAO;
    }          

    /**
     * Implementation of {@code SAMLUserDetailsService}. Parses user details from given 
     * SAML credential object.
     */
    @Override
    public Object loadUserBySAML(SAMLCredential credential)
    {
		PortalUserDetails toReturn = null;

		String userId = null;
		String name = null;
		// get userid and name: iterate over attributes searching for "mail" and "displayName":
        for (Attribute cAttribute : credential.getAttributes()) {
        	log.debug("loadUserBySAML(), parsing attribute - " + cAttribute.toString());
        	log.debug("loadUserBySAML(), parsing attribute - " + cAttribute.getName());
        	log.debug("loadUserBySAML(), parsing attribute - " + credential.getAttributeAsString(cAttribute.getName()));
        	if (userId == null && cAttribute.getName().equals("mail"))
        	{
        		userId = credential.getAttributeAsString(cAttribute.getName());
        		//userid = credential.getNameID().getValue(); needed to support OneLogin...?? Although with OneLogin we haven't gotten this far yet...
        	}
        	else if (name == null && cAttribute.getName().equals("displayName"))
        	{
        		name = credential.getAttributeAsString(cAttribute.getName());
        	}
        }

		//check if this user exists in our DB
		try {
            log.debug("loadUserDetails(), IDP successfully authenticated user, userid: " + userId);
            log.debug("loadUserDetails(), now attempting to fetch portal user authorities for userid: " + userId);
            
            //try to find user in DB. If not found, will go to catch (EmptyResultDataAccessException) below:
            User user = portalUserDAO.getPortalUser(userId);
        	if (user.isEnabled()) {
                log.debug("loadUserDetails(), user is enabled; attempting to fetch portal user authorities, userid: " + userId);

                UserAuthorities authorities = portalUserDAO.getPortalUserAuthorities(userId);
                if (authorities != null) {
                    List<GrantedAuthority> grantedAuthorities =
                        AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));
                    //add granted authorities:
                    toReturn = new PortalUserDetails(userId, grantedAuthorities);
                    toReturn.setEmail(userId);
                    toReturn.setName(userId);
                } else {
                    log.debug("loadUserDetails(), user authorities is null, userid: " + userId + ". Depending on property always_show_study_group, "
                    		+ "he could still have default access (to PUBLIC studies)");
                	toReturn = new PortalUserDetails(userId, getInitialEmptyAuthoritiesList());
                    toReturn.setEmail(userId);
                    toReturn.setName(userId);
                }
        	} else {
        		//user WAS found in DB but has been actively disabled:
        		throw new UsernameNotFoundException("Error: Your user access to cBioPortal has been disabled");
        	}
    		return toReturn;
		}
		catch (EmptyResultDataAccessException er) {
			//user record not found at all in DB.
			//if user is not in DB but is successfully authenticated, just give him the default empty access list.
        	//Depending on GlobalProperties.getAlwaysShowStudyGroup() he could still have access to PUBLIC studies:
            log.debug("loadUserDetails(), user and user authorities is null, userid: " + userId + ". Depending on property always_show_study_group, "
            		+ "he could still have default access (to PUBLIC studies)");
        	return new PortalUserDetails(userId, getInitialEmptyAuthoritiesList());
		}
		catch (UsernameNotFoundException unnf) {
			//throw this exception, so that the user gets redirected to the error HTML page: 
			throw unnf;
		}
		catch (Exception e) {
			//other (unexpected) errors: just throw (will result in http 500 page with error message):
			log.error(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error during authentication parsing: " + e.getMessage());
		}
    }

    /**
     * Returns an initial empty authorities list.
     * 
     * @return
     */
    private List<GrantedAuthority> getInitialEmptyAuthoritiesList()
    {
        return AuthorityUtils.createAuthorityList(new String[0]);
    }
}

