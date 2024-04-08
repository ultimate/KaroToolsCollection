package ultimate.karoapi4j.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test cases copied from karo wrapper PHP to make sure, that the java implementation behaves the same
 */
public class KezzerTest
{
	@Test
	public void test_md5()
	{
		assertEquals("5eb63bbbe01eeed093cb22bb8f5acdc3", Kezzer.md5("hello world"));
	}
	
	@Test
    public void test_rnd_int()
    {
        Kezzer rnd = new Kezzer("a");

        assertEquals(969, rnd.rnd(0, 1000));
        assertEquals(137, rnd.rnd(0, 1000));
        assertEquals(389, rnd.rnd(0, 1000));
        assertEquals(747, rnd.rnd(0, 1000));
        assertEquals(94, rnd.rnd(0, 1000));
        assertEquals(670, rnd.rnd(0, 1000));
        assertEquals(997, rnd.rnd(0, 1000));
        assertEquals(73, rnd.rnd(0, 1000));
        assertEquals(22, rnd.rnd(0, 1000));
        assertEquals(189, rnd.rnd(0, 1000));
        assertEquals(668, rnd.rnd(0, 1000));
        assertEquals(974, rnd.rnd(0, 1000));
        assertEquals(458, rnd.rnd(0, 1000));
        assertEquals(967, rnd.rnd(0, 1000));
        assertEquals(759, rnd.rnd(0, 1000));
        assertEquals(176, rnd.rnd(0, 1000));
        assertEquals(217, rnd.rnd(0, 1000));
        assertEquals(9, rnd.rnd(0, 1000));
        assertEquals(618, rnd.rnd(0, 1000));
        assertEquals(706, rnd.rnd(0, 1000));
        assertEquals(788, rnd.rnd(0, 1000));
        assertEquals(397, rnd.rnd(0, 1000));
        assertEquals(663, rnd.rnd(0, 1000));
        assertEquals(220, rnd.rnd(0, 1000));
        assertEquals(19, rnd.rnd(0, 1000));
        assertEquals(353, rnd.rnd(0, 1000));
        assertEquals(464, rnd.rnd(0, 1000));
        assertEquals(909, rnd.rnd(0, 1000));
        assertEquals(822, rnd.rnd(0, 1000));
        assertEquals(945, rnd.rnd(0, 1000));
        assertEquals(718, rnd.rnd(0, 1000));
        assertEquals(120, rnd.rnd(0, 1000));
        assertEquals(102, rnd.rnd(0, 1000));
        assertEquals(57, rnd.rnd(0, 1000));
        assertEquals(806, rnd.rnd(0, 1000));
        assertEquals(767, rnd.rnd(0, 1000));
        assertEquals(230, rnd.rnd(0, 1000));
        assertEquals(123, rnd.rnd(0, 1000));
        assertEquals(130, rnd.rnd(0, 1000));
        assertEquals(379, rnd.rnd(0, 1000));
        assertEquals(299, rnd.rnd(0, 1000));
        assertEquals(842, rnd.rnd(0, 1000));
        assertEquals(672, rnd.rnd(0, 1000));
        assertEquals(687, rnd.rnd(0, 1000));
        assertEquals(620, rnd.rnd(0, 1000));
        assertEquals(836, rnd.rnd(0, 1000));
        assertEquals(944, rnd.rnd(0, 1000));
        assertEquals(553, rnd.rnd(0, 1000));
        assertEquals(863, rnd.rnd(0, 1000));
        assertEquals(429, rnd.rnd(0, 1000));
        assertEquals(556, rnd.rnd(0, 1000));
        assertEquals(77, rnd.rnd(0, 1000));
        assertEquals(438, rnd.rnd(0, 1000));
        assertEquals(93, rnd.rnd(0, 1000));
        assertEquals(748, rnd.rnd(0, 1000));
        assertEquals(868, rnd.rnd(0, 1000));
        assertEquals(119, rnd.rnd(0, 1000));
        assertEquals(62, rnd.rnd(0, 1000));
        assertEquals(490, rnd.rnd(0, 1000));
        assertEquals(404, rnd.rnd(0, 1000));
        assertEquals(811, rnd.rnd(0, 1000));
        assertEquals(450, rnd.rnd(0, 1000));
        assertEquals(627, rnd.rnd(0, 1000));
        assertEquals(273, rnd.rnd(0, 1000));
        assertEquals(286, rnd.rnd(0, 1000));
        assertEquals(617, rnd.rnd(0, 1000));
        assertEquals(497, rnd.rnd(0, 1000));
        assertEquals(538, rnd.rnd(0, 1000));
        assertEquals(589, rnd.rnd(0, 1000));
        assertEquals(210, rnd.rnd(0, 1000));
        assertEquals(845, rnd.rnd(0, 1000));
        assertEquals(998, rnd.rnd(0, 1000));
        assertEquals(117, rnd.rnd(0, 1000));
        assertEquals(68, rnd.rnd(0, 1000));
        assertEquals(305, rnd.rnd(0, 1000));
        assertEquals(41, rnd.rnd(0, 1000));
        assertEquals(722, rnd.rnd(0, 1000));
        assertEquals(503, rnd.rnd(0, 1000));
        assertEquals(721, rnd.rnd(0, 1000));
        assertEquals(965, rnd.rnd(0, 1000));
        assertEquals(594, rnd.rnd(0, 1000));
        assertEquals(110, rnd.rnd(0, 1000));
        assertEquals(903, rnd.rnd(0, 1000));
        assertEquals(723, rnd.rnd(0, 1000));
        assertEquals(487, rnd.rnd(0, 1000));
        assertEquals(163, rnd.rnd(0, 1000));
        assertEquals(867, rnd.rnd(0, 1000));
        assertEquals(539, rnd.rnd(0, 1000));
        assertEquals(84, rnd.rnd(0, 1000));
        assertEquals(600, rnd.rnd(0, 1000));
        assertEquals(354, rnd.rnd(0, 1000));
        assertEquals(611, rnd.rnd(0, 1000));
        assertEquals(852, rnd.rnd(0, 1000));
        assertEquals(343, rnd.rnd(0, 1000));
        assertEquals(711, rnd.rnd(0, 1000));
        assertEquals(906, rnd.rnd(0, 1000));
        assertEquals(220, rnd.rnd(0, 1000));
        assertEquals(134, rnd.rnd(0, 1000));
        assertEquals(95, rnd.rnd(0, 1000));
        assertEquals(840, rnd.rnd(0, 1000));
    }

	@Test
    public void test_rnd_double()
    {
        Kezzer rnd = new Kezzer("f");

        assertEquals(0.56105499655433, rnd.rnd(), 0.00000000000001);
        assertEquals(0.51462020322317, rnd.rnd(), 0.00000000000001);
        assertEquals(0.57670080315722, rnd.rnd(), 0.00000000000001);
        assertEquals(0.86254680252427, rnd.rnd(), 0.00000000000001);
        assertEquals(0.31680566592877, rnd.rnd(), 0.00000000000001);
        assertEquals(0.51482692095081, rnd.rnd(), 0.00000000000001);
        assertEquals(0.035092060626891, rnd.rnd(), 0.00000000000001);
        assertEquals(0.53575134909372, rnd.rnd(), 0.00000000000001);
        assertEquals(0.78173065547186, rnd.rnd(), 0.00000000000001);
        assertEquals(0.84179209618605, rnd.rnd(), 0.00000000000001);
        assertEquals(0.071989629393937, rnd.rnd(), 0.00000000000001);
        assertEquals(0.045957597618884, rnd.rnd(), 0.00000000000001);
        assertEquals(0.8672950482918, rnd.rnd(), 0.00000000000001);
        assertEquals(0.4066944586748, rnd.rnd(), 0.00000000000001);
        assertEquals(0.16673598669457, rnd.rnd(), 0.00000000000001);
        assertEquals(0.5811212271251, rnd.rnd(), 0.00000000000001);
        assertEquals(0.90474133004137, rnd.rnd(), 0.00000000000001);
        assertEquals(0.59811531151946, rnd.rnd(), 0.00000000000001);
        assertEquals(0.79767570897253, rnd.rnd(), 0.00000000000001);
        assertEquals(0.1060846282104, rnd.rnd(), 0.00000000000001);
        assertEquals(0.6393892655664, rnd.rnd(), 0.00000000000001);
        assertEquals(0.49030775927796, rnd.rnd(), 0.00000000000001);
        assertEquals(0.56971269196008, rnd.rnd(), 0.00000000000001);
        assertEquals(0.48387890338227, rnd.rnd(), 0.00000000000001);
        assertEquals(0.88831113391077, rnd.rnd(), 0.00000000000001);
        assertEquals(0.81955967985893, rnd.rnd(), 0.00000000000001);
        assertEquals(0.40275992306183, rnd.rnd(), 0.00000000000001);
        assertEquals(0.70140666487481, rnd.rnd(), 0.00000000000001);
        assertEquals(0.20439245842173, rnd.rnd(), 0.00000000000001);
        assertEquals(0.97454840586079, rnd.rnd(), 0.00000000000001);
        assertEquals(0.71100298333283, rnd.rnd(), 0.00000000000001);
        assertEquals(0.28933291912167, rnd.rnd(), 0.00000000000001);
        assertEquals(0.84100647040634, rnd.rnd(), 0.00000000000001);
        assertEquals(0.56868308785973, rnd.rnd(), 0.00000000000001);
        assertEquals(0.075614035067734, rnd.rnd(), 0.00000000000001);
        assertEquals(0.19893529899725, rnd.rnd(), 0.00000000000001);
        assertEquals(0.61536915333733, rnd.rnd(), 0.00000000000001);
        assertEquals(0.066226105814096, rnd.rnd(), 0.00000000000001);
        assertEquals(0.95227627841579, rnd.rnd(), 0.00000000000001);
        assertEquals(0.20114075855995, rnd.rnd(), 0.00000000000001);
        assertEquals(0.82736923450711, rnd.rnd(), 0.00000000000001);
        assertEquals(0.93594338763438, rnd.rnd(), 0.00000000000001);
        assertEquals(0.50634355429967, rnd.rnd(), 0.00000000000001);
        assertEquals(0.80588085620739, rnd.rnd(), 0.00000000000001);
        assertEquals(0.36467404192011, rnd.rnd(), 0.00000000000001);
        assertEquals(0.69285538613403, rnd.rnd(), 0.00000000000001);
        assertEquals(0.36747308281796, rnd.rnd(), 0.00000000000001);
        assertEquals(0.69201981690835, rnd.rnd(), 0.00000000000001);
        assertEquals(0.73187607857578, rnd.rnd(), 0.00000000000001);
        assertEquals(0.85594011564169, rnd.rnd(), 0.00000000000001);
        assertEquals(0.38187370864465, rnd.rnd(), 0.00000000000001);
        assertEquals(0.12069927549646, rnd.rnd(), 0.00000000000001);
        assertEquals(0.13204082540961, rnd.rnd(), 0.00000000000001);
        assertEquals(0.065438222841877, rnd.rnd(), 0.00000000000001);
        assertEquals(0.21352479136938, rnd.rnd(), 0.00000000000001);
        assertEquals(0.41445889483945, rnd.rnd(), 0.00000000000001);
        assertEquals(0.34882157342613, rnd.rnd(), 0.00000000000001);
        assertEquals(0.26303453309407, rnd.rnd(), 0.00000000000001);
        assertEquals(0.84581943921935, rnd.rnd(), 0.00000000000001);
        assertEquals(0.94497972809793, rnd.rnd(), 0.00000000000001);
        assertEquals(0.24070439928995, rnd.rnd(), 0.00000000000001);
        assertEquals(0.37867725743199, rnd.rnd(), 0.00000000000001);
        assertEquals(0.60654615523521, rnd.rnd(), 0.00000000000001);
        assertEquals(0.39389671080976, rnd.rnd(), 0.00000000000001);
        assertEquals(0.43690344735117, rnd.rnd(), 0.00000000000001);
        assertEquals(0.31732867125892, rnd.rnd(), 0.00000000000001);
        assertEquals(0.41803093355308, rnd.rnd(), 0.00000000000001);
        assertEquals(0.96258456662014, rnd.rnd(), 0.00000000000001);
        assertEquals(0.61874326274677, rnd.rnd(), 0.00000000000001);
        assertEquals(0.029241355847447, rnd.rnd(), 0.00000000000001);
        assertEquals(0.27764066485979, rnd.rnd(), 0.00000000000001);
        assertEquals(0.7740642350405, rnd.rnd(), 0.00000000000001);
        assertEquals(0.94283325459389, rnd.rnd(), 0.00000000000001);
        assertEquals(0.55260083893123, rnd.rnd(), 0.00000000000001);
        assertEquals(0.56605674226078, rnd.rnd(), 0.00000000000001);
        assertEquals(0.77193292013257, rnd.rnd(), 0.00000000000001);
        assertEquals(0.89710834846179, rnd.rnd(), 0.00000000000001);
        assertEquals(0.63541294183039, rnd.rnd(), 0.00000000000001);
        assertEquals(0.065037549716024, rnd.rnd(), 0.00000000000001);
        assertEquals(0.86602060429637, rnd.rnd(), 0.00000000000001);
        assertEquals(0.97288935933272, rnd.rnd(), 0.00000000000001);
        assertEquals(0.28744258612486, rnd.rnd(), 0.00000000000001);
        assertEquals(0.38056383808772, rnd.rnd(), 0.00000000000001);
        assertEquals(0.6747682842184, rnd.rnd(), 0.00000000000001);
        assertEquals(0.096942321134783, rnd.rnd(), 0.00000000000001);
        assertEquals(0.14786430179385, rnd.rnd(), 0.00000000000001);
        assertEquals(0.78699733904751, rnd.rnd(), 0.00000000000001);
        assertEquals(0.53859890961736, rnd.rnd(), 0.00000000000001);
        assertEquals(0.18291702602939, rnd.rnd(), 0.00000000000001);
        assertEquals(0.2271256689119, rnd.rnd(), 0.00000000000001);
        assertEquals(0.38051564079404, rnd.rnd(), 0.00000000000001);
        assertEquals(0.61104082318525, rnd.rnd(), 0.00000000000001);
        assertEquals(0.079839930775705, rnd.rnd(), 0.00000000000001);
        assertEquals(0.25698680098014, rnd.rnd(), 0.00000000000001);
        assertEquals(0.26840606698612, rnd.rnd(), 0.00000000000001);
        assertEquals(0.72712024177981, rnd.rnd(), 0.00000000000001);
        assertEquals(0.42422712926372, rnd.rnd(), 0.00000000000001);
        assertEquals(0.61127870220486, rnd.rnd(), 0.00000000000001);
        assertEquals(0.53298479319221, rnd.rnd(), 0.00000000000001);
        assertEquals(0.66716622437844, rnd.rnd(), 0.00000000000001);
    }
}
