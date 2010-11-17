/**
 * Copyright (c) 2002-2010 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.server.webadmin.rest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;
import org.neo4j.server.rest.domain.JsonHelper;

public class AdminPropertiesServiceTest
{
    @Test
    public void simpleQueryShouldReturn200AndTheExpectedValue() throws Exception
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        config.setProperty( "org.neo4j.server.webadmin.foo", "bar" );
        UriInfo mockUri = mock( UriInfo.class );
        when( mockUri.getBaseUri() ).thenReturn( new URI( "http://peteriscool.com:6666/" ) );

        AdminPropertiesService adminPropertiesService = new AdminPropertiesService( mockUri, config );

        Response response = adminPropertiesService.getValue( "foo" );
        assertThat( response.getStatus(), is( 200 ) );
        assertThat( (String) response.getEntity(), containsString( "bar" ) );
    }

    @Test
    public void shouldSupportLegacyWebAdminUris() throws URISyntaxException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        String managementUri = "http://neo-is-awesome.se/manage";
        config.setProperty( "org.neo4j.server.webadmin.management.uri", managementUri );
        String dataUri = "http://jimsucks.com/data";
        config.setProperty( "org.neo4j.server.webadmin.data.uri", dataUri );

        UriInfo mockUri = mock( UriInfo.class );
        when( mockUri.getBaseUri() ).thenReturn( new URI( "http://peteriscool.com:6666/foo/bar?awesome=true" ) );

        AdminPropertiesService adminPropertiesService = new AdminPropertiesService( mockUri, config );

        Response response = adminPropertiesService.getValue( "neo4j-servers" );

        assertIsValidJson( response.getEntity().toString() );
        assertThat( (String) response.getEntity(), containsString( managementUri ) );
        assertThat( (String) response.getEntity(), containsString( dataUri ) );
    }

    private void assertIsValidJson( String entity )
    {
        JsonHelper.jsonToMap( entity );
    }

    @Test
    public void shouldYieldUndefinedForUnknownProperties() throws URISyntaxException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        UriInfo mockUri = mock( UriInfo.class );
        when( mockUri.getBaseUri() ).thenReturn( new URI( "http://peteriscool.com:6666/" ) );

        AdminPropertiesService adminPropertiesService = new AdminPropertiesService( mockUri, config );

        Response response = adminPropertiesService.getValue( "foo" );

        assertThat( (String) response.getEntity(), containsString( "undefined" ) );
    }


    @Test
    public void shouldAppendSlashToDataUriIfMissing() throws URISyntaxException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        String unterminatedUri = "http://foo:22/get/yer/data/here";
        config.setProperty( "org.neo4j.server.webadmin.data.uri", unterminatedUri );

        UriInfo mockUri = mock( UriInfo.class );
        when( mockUri.getBaseUri() ).thenReturn( new URI( "http://peteriscool.com:6666/" ) );

        AdminPropertiesService adminPropertiesService = new AdminPropertiesService( mockUri, config );

        Response response = adminPropertiesService.getValue( "data.uri" );

        assertThat( (String) response.getEntity(), containsString( unterminatedUri + "/" ) );
    }

    @Test
    public void shouldAppendSlashToManagementUriIfMissing() throws URISyntaxException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        String unterminatedUri = "http://foo:22/get/yer/data/here";
        config.setProperty( "org.neo4j.server.webadmin.management.uri", unterminatedUri );

        UriInfo mockUri = mock( UriInfo.class );
        when( mockUri.getBaseUri() ).thenReturn( new URI( "http://peteriscool.com:6666/" ) );

        AdminPropertiesService adminPropertiesService = new AdminPropertiesService( mockUri, config );

        Response response = adminPropertiesService.getValue( "management.uri" );

        assertThat( (String) response.getEntity(), containsString( unterminatedUri + "/" ) );
    }

    @Test
    public void shouldAppendSlashToDataUriIfMissingWithinNeo4jServerProperty() throws URISyntaxException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();
        String unterminatedDataUri = "http://foo:22/get/yer/data/here";
        config.setProperty( "org.neo4j.server.webadmin.data.uri", unterminatedDataUri );
        String unterminatedManagementUri = "http://neo-is-awesome.se/manage";
        config.setProperty( "org.neo4j.server.webadmin.management.uri", unterminatedManagementUri );

        UriInfo mockUri = mock( UriInfo.class );
        when( mockUri.getBaseUri() ).thenReturn( new URI( "http://peteriscool.com:6666/" ) );

        AdminPropertiesService adminPropertiesService = new AdminPropertiesService( mockUri, config );

        Response response = adminPropertiesService.getValue( "neo4j-servers" );

        assertThat( (String) response.getEntity(), containsString( unterminatedDataUri + "/" ) );
        assertThat( (String) response.getEntity(), containsString( unterminatedManagementUri + "/" ) );
    }

    @Test
    public void restEndpointShouldBeIdentifiedWithCorrectKey() throws URISyntaxException
    {
        PropertiesConfiguration config = new PropertiesConfiguration();

        final String rootHostUri = "http://peteriscool.com:6666";
        final String baseUriAsString = rootHostUri + "/db/manage/properties/";
        UriInfo mockUri = mock( UriInfo.class );
        when( mockUri.getBaseUri() ).thenReturn( new URI( baseUriAsString ) );

        AdminPropertiesService adminPropertiesService = new AdminPropertiesService( mockUri, config );

        Response response = adminPropertiesService.getValue( "neo4j-servers" );

        assertThat( (String) response.getEntity(), containsString( "\"url\" : \"" + rootHostUri + "/db/data/\"" ) );
        assertThat( (String) response.getEntity(), containsString( "\"manageUrl\" : \"" + rootHostUri + "/db/manage/\"" ) );

    }
}
