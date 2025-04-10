/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package it.usr.web.handler;

import it.usr.web.producer.AppLogger;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;
import jakarta.inject.Inject;
import org.slf4j.Logger;

/**
 *
 * @author riccardo.iovenitti
 */
public class UsrAjaxExceptionHandlerFactory extends ExceptionHandlerFactory {
    @Inject
    @AppLogger
    private Logger logger;
    private ExceptionHandlerFactory exceptionHandlerFactory;

    public UsrAjaxExceptionHandlerFactory() {
    }

    public UsrAjaxExceptionHandlerFactory(ExceptionHandlerFactory exceptionHandlerFactory) {
        this.exceptionHandlerFactory = exceptionHandlerFactory;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new UsrAjaxExceptionHandler(exceptionHandlerFactory.getExceptionHandler(), logger);
    }
}
