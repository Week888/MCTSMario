@echo off
cd ./bin
echo Starting Combination MiniStats2

REM class seed rsl cp USE_SOFTMAX USE_MACRO_ACTIONS USE_PARTIAL_EXPANSION USE_ROULETTE_WHEEL_SELECTION USE_HOLE_DETECTION USE_LIMITED_ACTIONS

echo %DATE% %TIME%

echo Testing UCT
java itu.ejuuragr.MiniStats2 itu.ejuuragr.UCT.EnhancementTester 0 6 0.25 0.125 0 0 0 0 0 0 >> "Enhancement UCT.txt"
echo Completed test (1/2)

echo %DATE% %TIME%

echo Testing Macro
java itu.ejuuragr.MiniStats2 itu.ejuuragr.UCT.EnhancementTester 0 6 0.25 0.125 1 0 1 1 1 1 >> "Enhancement Minus Macro.txt"
echo Completed test (2/2)

echo %DATE% %TIME%
echo ALL DONE
echo ALL DONE
echo ALL DONE
pause