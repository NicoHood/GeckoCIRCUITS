/*  This file is part of GeckoCIRCUITS. Copyright (C) ETH Zurich, Gecko-Simulations GmbH
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
package ch.technokrat.gecko.geckocircuits.control.calculators;

/**
 *
 * @author andreas
 */
public final class GainCalculator extends AbstractSingleInputSingleOutputCalculator {

    private double _gain;

    public GainCalculator(final double gain) {
        super();
        setGain(gain);
    }

    @Override
    public void berechneYOUT(final double deltaT) {
        _outputSignal[0][0] = _gain * _inputSignal[0][0];
    }

    public void setGain(final double gain) {
        _gain = gain;
    }
}
