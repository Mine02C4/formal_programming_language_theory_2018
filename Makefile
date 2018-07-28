COINS_DIR := coins-1.5-en
LIR_OPT := divex/printflow/peephole2/printflow/constant_folding/printflow

.PHONY: coins test test/test.out

coins:
	cd $(COINS_DIR) && ./build.sh

test: test/test.out
	./$<

test/test.out: test/test.c
	java -classpath $(COINS_DIR)/classes coins.driver.Driver \
		-I$(COINS_DIR)/lang/c/include -I$(COINS_DIR)/lang/c/include/samples \
		$< -coins:assembler=as,target=x86_64,lir-opt=$(LIR_OPT) -o $@
