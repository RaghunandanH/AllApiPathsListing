package org.apache.fineract.portfolio.group.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.model.AbstractSubResourceMethod;
import com.sun.jersey.server.impl.modelapi.annotation.IntrospectionModeller;

@Path("/")
@Component
@Scope("singleton")
public class ResourceListingResource
{
    @SuppressWarnings("deprecation")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayNode showAll( @Context Application application,
                             @Context HttpServletRequest request)
    {
        String basePath = request.getRequestURL().toString();
 
        ObjectNode root = JsonNodeFactory.instance.objectNode();
        com.fasterxml.jackson.databind.node.ArrayNode resources = JsonNodeFactory.instance.arrayNode();
 
        root.put( "resources", resources );
 
        for ( Class<?> aClass : application.getClasses() )
        {
            if ( isAnnotatedResourceClass( aClass ) )
            {
                AbstractResource resource = IntrospectionModeller.createResource( aClass );
                ObjectNode resourceNode = JsonNodeFactory.instance.objectNode();
                String uriPrefix = resource.getPath().getValue();
 
                for ( AbstractSubResourceMethod srm : resource.getSubResourceMethods() )
                {
                    String uri = uriPrefix + "/" + srm.getPath().getValue();
                    addTo( resourceNode, uri, srm,  basePath + uri);
                }
 
                for ( AbstractResourceMethod srm : resource.getResourceMethods() )
                {
                    addTo( resourceNode, uriPrefix, srm,  basePath + uriPrefix );
                }
 
                resources.add( resourceNode );
            }
 
        }
        System.out.println(resources);
        return resources;
    }
 
    @SuppressWarnings("deprecation")
    private void addTo( ObjectNode resourceNode, String uriPrefix, AbstractResourceMethod srm, String path )
    {
        if ( resourceNode.get( uriPrefix ) == null )
        {
            ObjectNode inner = JsonNodeFactory.instance.objectNode();
            inner.put("path", path);
            inner.put("verbs", JsonNodeFactory.instance.arrayNode());
            resourceNode.put( uriPrefix, inner );
        }
 
        ((com.fasterxml.jackson.databind.node.ArrayNode) resourceNode.get( uriPrefix ).get("verbs")).add( srm.getHttpMethod() );
    }
 
 
    @SuppressWarnings("unchecked")
    private boolean isAnnotatedResourceClass( Class rc )
    {
        if ( rc.isAnnotationPresent( Path.class ) )
        {
            return true;
        }
 
        for ( Class i : rc.getInterfaces() )
        {
            if ( i.isAnnotationPresent( Path.class ) )
            {
                return true;
            }
        }
 
        return false;
    }
 
}

