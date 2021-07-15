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

import ch.technokrat.gecko.geckocircuits.circuit.AbstractTypeInfo;
import ch.technokrat.gecko.geckocircuits.circuit.CircuitSourceType;
import ch.technokrat.gecko.geckocircuits.circuit.CircuitTypeInfo;
import ch.technokrat.gecko.geckocircuits.circuit.TerminalRelativePosition;
import ch.technokrat.gecko.i18n.resources.I18nKeys;
import java.awt.Graphics2D;
import java.awt.Window;
import java.util.Arrays;
import java.util.List;

// Maschinenmodell 
// Asynchronmaschine allg. 
public final class MotorInductionMachine extends AbstractMotorIM {
    public static AbstractTypeInfo TYPE_INFO = new CircuitTypeInfo(MotorInductionMachine.class, "IM", I18nKeys.INDUCTION_MACHINE);

    private int drMpix = 3;
    private double Lls = 1e-3;
    private double Rr = 1.0, Llr = 1.8e-3;
    // interne Variablen: 
    private double imd0 = 0, imq0 = 0;
    private double ira = 0, irb = 0, ira0 = 0, irb0 = 0;  // Rotor-Phasenstroeme
    private AbstractCurrentSource _controlledSource3;
    private AbstractCurrentSource _controlledSource4;
    private AbstractResistor _resistor;    

    @Override
    void setTerminals() {
        XIN.add(new TerminalRelativePosition(this, -2, 1));
        XIN.add(new TerminalRelativePosition(this, -2, 0));
        XIN.add(new TerminalRelativePosition(this, -2, -1));

        YOUT.add(new TerminalRelativePosition(this, 2, 1));
        YOUT.add(new TerminalRelativePosition(this, 2, 0));
        YOUT.add(new TerminalRelativePosition(this, 2, -1));
    }

    @Override
    void setSubCircuit() {
        super.setSubCircuit();
        // Eingangsstromquelle fuer iax(t) --> 
        _controlledSource3 = (AbstractCurrentSource) fabricHiddenSub(CircuitTyp.LK_I, this);
        // Eingangsstromquelle fuer icx(t) --> 
        _controlledSource4 = (AbstractCurrentSource) fabricHiddenSub(CircuitTyp.LK_I, this);
        // hochohmiger Widerstand zur Anbindung des Rotorkreises --> 
        _resistor = (AbstractResistor) fabricHiddenSub(CircuitTyp.LK_R, this);

        _controlledSource3.sourceType.setValueWithoutUndo(CircuitSourceType.QUELLE_SIGNALGESTEUERT);
        _controlledSource4.sourceType.setValueWithoutUndo(CircuitSourceType.QUELLE_SIGNALGESTEUERT);
        _resistor._resistance.setValueWithoutUndo(1e8);
                
        _controlledSource3.setInputTerminal(0, YOUT.get(0));
        _controlledSource3.setOutputTerminal(0, YOUT.get(1));

        _controlledSource4.setInputTerminal(0, YOUT.get(2));
        _controlledSource4.setOutputTerminal(0, YOUT.get(1));

        _resistor.setInputTerminal(0, XIN.get(1));
        _resistor.setOutputTerminal(0, YOUT.get(1));
    }

    public int getIndexForLoadTorque() {
        return 15;
    }  
    

    // Initialisiereung nach INIT&START --> 
    @Override
    public void setzeParameterZustandswerteAufNULL() {
        super.setzeParameterZustandswerteAufNULL();
        
        Lls = parameter[18];
        Rr = parameter[19];
        Llr = parameter[20];
        _magnetizingInductance = parameter[21];
        isa0 = parameter[22];
        isb0 = parameter[23];        
        
        isd0 = isa0;        
        isq0 = 1 / Math.sqrt(3) * (isa0 + 2 * isb0);
        imd0 = (psisd0 - Lls * isd0) / _magnetizingInductance;
        imq0 = (psisq0 - Lls * isq0) / _magnetizingInductance;
        ird0 = imd0 - isd0;
        irq0 = imq0 - isq0;
        isd = isd0;
        isq = isq0;
        ird = ird0;
        irq = irq0;
        _controlledAnchorSourceA.parameter[1] = isa0;
        _controlledAnchorSourceC.parameter[1] = -(isa0 + isb0);
        _controlledSource3.parameter[1] = ira0;
        _controlledSource4.parameter[1] = -(ira0 + irb0);
    }

    @Override
    public List<String> getParameterStringIntern() {
        return Arrays.asList("isa [A]", "isb [A]", "isc [A]", "omega", "n [rpm]", "theta [rad]", "Tel [Nm]");
    }    

    @Override
    void updateOldSolverParameters() {
        super.updateOldSolverParameters();        
        parameter[7] = psisd;
        parameter[8] = psisq;        
    }                
    

    @Override
    void calculateMotorEquations(double deltaT, double time) {
        double usab = _controlledAnchorSourceA.parameter[7];
        double usbc = -_controlledAnchorSourceC.parameter[7];        
        double urab = _controlledSource3.parameter[7];
        double urbc = -_controlledSource4.parameter[7];
        //------
        // Block 'usab,usbc -> udq': 
        double usd = 2 * usab / 3 + usbc / 3;
        
        /*counter++;
        System.out.println("iii " + usd + " " + usab + " " + usbc);
        if(counter > 2) {
            System.exit(4);
        }*/
        
        double usq = usbc / Math.sqrt(3);
        // Block 'vrdq': 
        double urd = 2.0 / 3.0 * (urab * Math.cos(_thetaElectric) - urbc * Math.cos(_thetaElectric - 2 * Math.PI / 3));
        double urq = 2.0 / 3.0 * (urab * Math.sin(_thetaElectric) - urbc * Math.sin(_thetaElectric - 2 * Math.PI / 3));                
        
        // Bloecke fuer magnetische Fluss-Berechnungen: 
        double psimd = _magnetizingInductance * (isd + ird);
        double psimq = _magnetizingInductance * (isq + irq);
        psisd = Lls * isd + psimd;
        psisq = Lls * isq + psimq;
        double psird = Llr * ird + psimd;
        double psirq = Llr * irq + psimq;
        double upsird = -_omegaElectric * psirq;
        double upsirq = _omegaElectric * psird;
        //------
        // Dgl. des Asynchron-Trafo-ESBs --> isdq(usdq,urdq,upsirdq), irdq(usdq,urdq,upsirdq): 
        double kk1d = usd + isd0 * (Lls + _magnetizingInductance) / deltaT + ird0 * _magnetizingInductance / deltaT;                
        
        double kk2d = urd + upsird + isd0 * _magnetizingInductance / deltaT + ird0 * (Llr + _magnetizingInductance) / deltaT;
        double kk1q = usq + isq0 * (Lls + _magnetizingInductance) / deltaT + irq0 * _magnetizingInductance / deltaT;
        double kk2q = urq + upsirq + isq0 * _magnetizingInductance / deltaT + irq0 * (Llr + _magnetizingInductance) / deltaT;
        double kk3 = (_magnetizingInductance / deltaT) * (_magnetizingInductance / deltaT) - (Rr + (Llr + _magnetizingInductance) / deltaT) * (_statorResistance + (Lls + _magnetizingInductance) / deltaT);
        double kk4 = -(_magnetizingInductance / deltaT) * (_magnetizingInductance / deltaT) + (Rr + (Llr + _magnetizingInductance) / deltaT) * (_statorResistance + (Lls + _magnetizingInductance) / deltaT);
        isd = (kk1d * (Rr + (Llr + _magnetizingInductance) / deltaT) - kk2d * (_magnetizingInductance / deltaT)) / kk4;
        isq = (kk1q * (Rr + (Llr + _magnetizingInductance) / deltaT) - kk2q * (_magnetizingInductance / deltaT)) / kk4;
        ird = (kk1d * (_magnetizingInductance / deltaT) - kk2d * (_statorResistance + (Lls + _magnetizingInductance) / deltaT)) / kk3;                        
        
        irq = (kk1q * (_magnetizingInductance / deltaT) - kk2q * (_statorResistance + (Lls + _magnetizingInductance) / deltaT)) / kk3;
                
                
    }
    
    static int counter = 0;

    @Override
    void updateSourceParameters() {
        ira = ird * Math.cos(_thetaElectric) + irq * Math.sin(_thetaElectric);
        irb = ird * Math.cos(_thetaElectric + 2 * Math.PI / 3) + irq * Math.sin(_thetaElectric + 2 * Math.PI / 3);                                
        
        // NO! call to super.updateSourceParameters!
        // Block 'isa,isb': 
        isa = isd;
        isb = 0.5 * (-isd + Math.sqrt(3) * isq);
        isc = 0.5 * (-isd - Math.sqrt(3) * isq);
        _controlledAnchorSourceA.parameter[1] = isa;
        _controlledAnchorSourceC.parameter[1] = -(isa + isb);  // isc
        _controlledSource3.parameter[1] = ira;
        _controlledSource4.parameter[1] = -(ira + irb);  // irc 
    }

    @Override
    void updateHistoryVariables() {
        super.updateHistoryVariables();
        isd0 = isd;
        isq0 = isq;
        ird0 = ird;
        irq0 = irq;        
    }
            
    
    
    
    
    @Override
    protected void drawOnTop(Graphics2D graphics) {
        graphics.drawOval((int) (-dpix * RADIUS_MOTOR_SYMBOL) + drMpix, (int) (-dpix * RADIUS_MOTOR_SYMBOL) + drMpix, (int) (dpix * 2 * RADIUS_MOTOR_SYMBOL) - 2 * drMpix, (int) (dpix * 2 * RADIUS_MOTOR_SYMBOL) - 2 * drMpix);
    }
    

    @Override
    protected Window openDialogWindow() {
        return new MotorInductionMachineDialog(this);
    }

    @Override
    protected void drawConnectorLines(Graphics2D graphics) {
        super.drawConnectorLines(graphics);
        super.drawRightUpperTerminalLine(graphics);
        super.drawRightMidTerminalLine(graphics);
        super.drawRightLowerTerminalLine(graphics);
    }             
}
