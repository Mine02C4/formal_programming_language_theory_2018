COINS_DIR := coins-1.5-en
LIR_OPT := divex/printflow/peephole_cse/printflow/constant_folding/printflow
AV1_LIR_OPT := divex/printflow/av/printflow
AV2_LIR_OPT := divex/printflow/av_cse/printflow

.PHONY: coins test test/*

coins:
	cd $(COINS_DIR) && ./build.sh

test: test/test.out
	./$<

test_av: test/test_av1.out test/test_av2.out
	./test/test_av1.out | tee test_av1.txt
	./test/test_av2.out | tee test_av2.txt
	diff test_av1.txt test_av2.txt

test/test.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(LIR_OPT) -o $@

test/test_av1.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(AV1_LIR_OPT) -o $@

test/test_av2.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(AV2_LIR_OPT) -o $@

