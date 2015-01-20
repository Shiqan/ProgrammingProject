/**
 * 
 */
package nl.ferron.saan;

import android.app.Activity;
import android.content.Context;

/**
 * @author Ferron
 *
 */
public class AccomplishmentsOutbox extends Activity {
        public boolean mPrimeAchievement = false;
        public boolean mDieAchievement = false;
        public boolean mHalfwayAchievement = false;
        public boolean mLeetAchievement = false;
        public int mBoredSteps = 0;

        boolean isEmpty() {
            return !mPrimeAchievement && !mHalfwayAchievement && !mLeetAchievement 
            		&& !mDieAchievement && mBoredSteps == 0;
        }

        public void saveLocal(Context ctx) {
            /* TODO: This is left as an exercise. To make it more difficult to cheat,
             * this data should be stored in an encrypted file! And remember not to
             * expose your encryption key (obfuscate it by building it from bits and
             * pieces and/or XORing with another string, for instance). */
        }

        public void loadLocal(Context ctx) {
            /* TODO: This is left as an exercise. Write code here that loads data
             * from the file you wrote in saveLocal(). */
        }
    
}
