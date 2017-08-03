package com.petpet.c3po.command;

import com.petpet.c3po.common.Constants;
import com.petpet.c3po.controller.Controller;
import com.petpet.c3po.parameters.DeconflictParams;
import com.petpet.c3po.parameters.Params;
import com.petpet.c3po.parameters.ProfileParams;
import com.petpet.c3po.utils.Configurator;
import com.petpet.c3po.utils.exceptions.C3POConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by artur on 05/03/16.
 */
public class ResolveConflictsCommand extends AbstractCLICommand implements Command{

    private static final Logger LOG = LoggerFactory.getLogger(ProfileCommand.class);

    /**
     * The profile parameter passed on the command line.
     */
    private DeconflictParams params;

    @Override
    public void setParams(Params params) {
        if ( params != null && params instanceof DeconflictParams ) {
            this.params = (DeconflictParams) params;
        }

    }

    @Override
    public void execute() {

        final long start = System.currentTimeMillis();

        final Configurator configurator = Configurator.getDefaultConfigurator();
        //configurator.configure();

        Map<String, String> options = new HashMap<String, String>();
        options.put( Constants.OPT_COLLECTION_NAME, this.params.getCollection() );
        options.put( Constants.CNF_DROOLS_PATH, this.params.getLocation() );

        Controller ctrl = new Controller( configurator );

        try {
            ctrl.resolveConflicts(options);
        } catch ( Exception e ) {
            LOG.error( e.getMessage() );
            return;

        } finally {
            cleanup();
        }

        final long end = System.currentTimeMillis();
        this.setTime( end - start );


    }
}
