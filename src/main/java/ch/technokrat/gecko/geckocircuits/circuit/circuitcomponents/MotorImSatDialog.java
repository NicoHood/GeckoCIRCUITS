/*  This file is part of GeckoCIRCUITS. Copyright (C) ETH Zurich, Gecko-Simulations AG
 *
 *  GeckoCIRCUITS is free software: you can redistribute it and/or modify it under 
 *  the terms of the GNU General Public License as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  GeckoCIRCUITS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 *  PURPOSE.  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  GeckoCIRCUITS.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.technokrat.gecko.geckocircuits.circuit.circuitcomponents;

import ch.technokrat.gecko.geckocircuits.allg.UserParameter;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;
import javax.swing.JPanel;

/**
 *
 * @author andy
 * Asynchronmaschine allgemein mit saettigbarer Induktivitaet -->
 */
class MotorImSatDialog extends AbstractMotorDialog<MotorImSat> {

    public MotorImSatDialog(final MotorImSat parent) {
        super(parent);
    }
    
    @Override
    List<UserParameter<Double>> getDialogSortedParameters() {
        return Arrays.asList(element._frictionParameter, element._inertiaParameter, 
                element._polePairsParameter, element._statorResistancePar,
                element.statorLeakageInductance, element._unsaturatedMagnetizingInductance, 
                element._saturatedMagnetizingInductance, 
                element._fluxSaturationTransition, element._tightnessOfSaturationTransition,
                element.rotorResistance, element.rotorLeakageInductance,                
                element._initialRotationalSpeed, element._initialRotorPosition, 
                element.initialStatorCurrentA, element.initialStatorCurrentB, 
                element.initialStatorFluxD, element.initialStatorFluxQ);
    }
    
    @Override
    List<UserParameter<Double>> getInitPanelParameters() {
        return Arrays.asList(element._initialRotationalSpeed, element._initialRotorPosition, 
                element.initialStatorCurrentA, element.initialStatorCurrentB, 
                element.initialStatorFluxD, element.initialStatorFluxQ);
    }
    

    @Override
    JPanel buildPanelInitParameter() {        
        return super.buildPanelParameters(11, 17, new int[]{2,5}, false);        
    }

    @Override
    JPanel buildPanelParameters() {
        return super.buildPanelParameters(0, 11, new int[]{3,10,13}, true);        
    }   
        
}
